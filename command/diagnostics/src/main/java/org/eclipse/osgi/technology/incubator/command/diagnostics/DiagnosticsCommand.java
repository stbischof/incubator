/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Stefan Bischof - initial
 */
package org.eclipse.osgi.technology.incubator.command.diagnostics;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.osgi.technology.incubator.command.diagnostics.util.Export;
import org.eclipse.osgi.technology.incubator.command.diagnostics.util.FilterListener;
import org.eclipse.osgi.technology.incubator.command.diagnostics.util.Search;
import org.eclipse.osgi.technology.incubator.command.util.GlobFilter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

public class DiagnosticsCommand implements Closeable {

    private final BundleContext context;
    private final FilterListener fl;

    public DiagnosticsCommand(BundleContext context, DTOFormatter dtof) {
        this.context = context;
        this.fl = new FilterListener(context);
    }

    @Override
    public void close() {
        fl.close();
    }

    @Descriptor("Show all requirements. Iterates over all (or one) bundles and gathers their requirements.")
    public List<Requirement> reqs(
            @Descriptor("Only show the requirements of the given bundle") @Parameter(names = { "-b",
                    "--bundle" }, absentValue = "*") GlobFilter bundle,
            @Descriptor("Only show the requirements when one of the given namespace matches. You can use wildcards.") GlobFilter... ns) {

        List<Requirement> reqs = new ArrayList<>();

        for (Bundle b : context.getBundles()) {
            if (!bundle.createMatcher(b.getSymbolicName()).matches()) {
                continue;
            }

            var wiring = b.adapt(BundleWiring.class);

            var requirements = wiring.getRequirements(null);
            for (Requirement r : requirements) {
                if (matches(ns, r.getNamespace())) {
                    reqs.add(r);
                }
            }
        }
        return reqs;
    }

    private boolean matches(GlobFilter[] ns, String namespace) {
        if (ns == null || ns.length == 0) {
            return true;
        }

        for (GlobFilter g : ns) {
            if (g.matches(namespace)) {

                return true;
            }
        }
        return false;
    }

    @Descriptor("Show all capabilities of all bundles. It is possible to list by bundle and/or by a specific namespace.")
    public List<Capability> caps(
            @Descriptor("Only show the capabilities of the given bundle") @Parameter(names = { "-b",
                    "--bundle" }, absentValue = "-1") long bundle,
            @Descriptor("""
                    Only show the capabilities when the given namespace matches. You can use wildcards. A number of namespaces are shortcutted:
                      p = osgi.wiring.package
                      i = osgi.wiring.identity
                      h = osgi.wiring.host
                      b = osgi.wiring.bundle
                      e = osgi.extender
                      s = osgi.service
                      c = osgi.contract""") @Parameter(names = {
                    "-n", "--namespace" }, absentValue = "*") String ns) {

        var nsg = shortcuts(ns);

        List<Capability> result = new ArrayList<>();

        for (Bundle b : context.getBundles()) {
            if (bundle != -1 && b.getBundleId() != bundle) {
                continue;
            }

            var wiring = b.adapt(BundleWiring.class);

            var capabilities = wiring.getCapabilities(null);
            for (Capability r : capabilities) {
                if (nsg.createMatcher(r.getNamespace()).matches()) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    @Descriptor("""
            Show bundles that are listening for certain services. This will check for the following fishy cases:\s
            * ? – No matching registered service found
            * ! – Matching registered service found in another classpace
            The first set show is the set of bundle that register such a service in the proper class space. The second \
            set (only shown when not empty) shows the bundles that have a registered service for this but are not \
            compatible because they are registered in another class space""")
    public List<Search> wanted(
            @Descriptor("If specified will only show for the given bundle") @Parameter(names = { "-b",
                    "--bundle" }, absentValue = "-1") long exporter,
            @Descriptor("If specified, this glob expression must match the name of the service class/interface name") @Parameter(names = {
                    "-n", "--name" }, absentValue = "*") GlobFilter name)
            throws InvalidSyntaxException {
        List<Search> searches = new ArrayList<>();
        synchronized (fl) {
            for (Map.Entry<String, List<BundleContext>> e : fl.listenerContexts.entrySet()) {

                var serviceName = e.getKey();

                if (!name.createMatcher(serviceName).matches()) {
                    continue;
                }

                ServiceReference<?> refs[] = context.getAllServiceReferences(serviceName, null);
                for (BundleContext bc : e.getValue()) {

                    if (exporter != -1 && exporter != bc.getBundle().getBundleId()) {
                        continue;
                    }

                    var wiring = bc.getBundle().adapt(BundleWiring.class);

                    var s = new Search();
                    s.serviceName = serviceName;
                    s.searcher = wiring.getRevision();

                    var classLoader = wiring.getClassLoader();
                    Class<?> type = load(classLoader, serviceName);

                    if (refs != null) {
                        for (ServiceReference<?> ref : refs) {
                            var registrar = ref.getBundle();

                            Class<?> registeredClass = load(registrar, serviceName);
                            var bundleId = registrar.getBundleId();
                            if (type == null || registeredClass == null || type == registeredClass) {
                                s.matched.add(bundleId);
                            } else {
                                s.mismatched.add(bundleId);
                            }
                        }
                    }
                    searches.add(s);
                }
            }
        }
        return searches;
    }

    @Descriptor("Show exported packages of all bundles that look fishy. Options are provided to filter for a specific bundle and/or the package name (glob). You can also specify -a for all packages")
    public Collection<Export> exports(
            @Descriptor("If specified will only show for the given bundle") @Parameter(names = { "-b",
                    "--bundle" }, absentValue = "-1") long exporter,
            @Descriptor("If specified, this glob expression must match the name of the service class/interface name") @Parameter(names = {
                    "-n", "--name" }, absentValue = "*") GlobFilter name,
            @Descriptor("Show all packages, not just the ones that look fishy") @Parameter(names = { "-a",
                    "--all" }, absentValue = "false", presentValue = "true") boolean all,
            @Descriptor("Check exports against private packages") @Parameter(names = { "-p",
                    "--private" }, absentValue = "false", presentValue = "true") boolean privatePackages) {
        Map<String, Export> map = new HashMap<>();

        var caps = caps(-1, PackageNamespace.PACKAGE_NAMESPACE);

        for (Capability c : caps) {
            var packageName = (String) c.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
            if (!name.createMatcher(packageName).matches()) {
                continue;
            }

            var resource = c.getResource();
            if (resource instanceof BundleRevision) {
                var bundle = ((BundleRevision) resource).getBundle();
                if (exporter != -1 && bundle.getBundleId() != exporter) {
                    continue;
                }
                var e = map.get(packageName);
                if (e == null) {
                    e = new Export(packageName);
                    map.put(packageName, e);
                }
                e.exporters.add(bundle.getBundleId());
            }

        }

        for (Export e : map.values()) {
            for (Bundle b : context.getBundles()) {

                if (e.exporters.contains(b.getBundleId())) {
                    continue;
                }

                if (hasPackage(b, e.pack)) {
                    e.privates.add(b.getBundleId());
                }
            }
        }

        if (all) {
            return map.values();
        }

        Set<Export> s = new HashSet<>(map.values());
        s.removeIf(e -> e.exporters.size() == 1 && e.privates.isEmpty());
        return s;
    }

    private boolean hasPackage(Bundle b, String pack) {
        var entries = b.findEntries(pack.replace('.', '/'), "*", false);
        return entries != null && entries.hasMoreElements();
    }

    private Class<?> load(Bundle bundle, String name) {
        return load(bundle.adapt(BundleWiring.class).getClassLoader(), name);
    }

    private Class<?> load(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (Exception e) {
            return null;
        }
    }

    private GlobFilter shortcuts(String ns) {
        switch (ns) {
        case "p":
            ns = "osgi.wiring.package";
            break;

        case "i":
            ns = "osgi.wiring.identity";
            break;
        case "h":
            ns = "osgi.wiring.host";
            break;
        case "b":
            ns = "osgi.wiring.bundle";
            break;
        case "e":
            ns = "osgi.extender";
            break;
        case "s":
            ns = "osgi.service";
            break;
        case "c":
            ns = "osgi.contract";
            break;
        }
        var nsg = new GlobFilter(ns);
        return nsg;
    }

}
