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
package org.eclipse.osgi.technology.incubator.command.osgi.framework;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.osgi.technology.incubator.command.util.DisplayUtil;
import org.eclipse.osgi.technology.incubator.command.util.GlobFilter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;

public class FrameworkCommands {

    private BundleContext context;

    public FrameworkCommands(BundleContext context, DTOFormatter formatter) {
        this.context = context;
        dtos(formatter);
    }

    void dtos(DTOFormatter f) {
        f.build(FrameworkDTO.class).inspect().fields("*").line().fields("*").part();
        f.build(BundleDTO.class).inspect().fields("*").line().fields("*").part();
        f.build(ServiceReferenceDTO.class).inspect().fields("*").line().fields("*").part();

        f.build(Bundle.class).inspect().method("bundleId").format("STATE", FrameworkCommands::state)
                .method("symbolicName").method("version").method("location")
                .format("LAST MODIFIED", b -> DisplayUtil.lastModified(b.getLastModified()))
                .format("servicesInUse", b -> b.getServicesInUse()).method("registeredServices")
                .format("HEADERS", Bundle::getHeaders).part()
                .as(b -> "[" + b.getBundleId() + "] " + b.getSymbolicName()).line().method("bundleId")
                .format("STATE", FrameworkCommands::state).method("symbolicName").method("version")
                .format("START LEVEL", this::startlevel)
                .format("LAST MODIFIED", b -> DisplayUtil.lastModified(b.getLastModified()));

        f.build(BundleDTO.class).inspect().fields("*").line().field("id").field("symbolicName").field("version")
                .field("state").part().as(b -> String.format("[%s]%s", b.id, b.symbolicName));

        f.build(ServiceReference.class).inspect().format("id", s -> getServiceId(s) + "")
                .format("objectClass", FrameworkCommands::objectClass)
                .format("bundle", s -> s.getBundle().getBundleId() + "")
                .format("usingBundles", s -> bundles(s.getUsingBundles())).format("properties", DisplayUtil::toMap)
                .line().format("id", s -> getServiceId(s) + "").format("bundle", s -> s.getBundle().getBundleId() + "")
                .format("service", FrameworkCommands::objectClass)
                .format("ranking", s -> s.getProperty(Constants.SERVICE_RANKING))
                .format("component", s -> s.getProperty("component.id"))
                .format("usingBundles", s -> bundles(s.getUsingBundles())).part()
                .as(s -> String.format("(%s) %s", getServiceId(s), objectClass(s)));

        f.build(BundleStartLevel.class).inspect().format("level", s -> s.getStartLevel() + "")
                .format("persistent", s -> s.isPersistentlyStarted())
                .format("act. policy", s -> s.isActivationPolicyUsed()).line()
                .format("level", s -> s.getStartLevel() + "").format("persistent", s -> s.isPersistentlyStarted())
                .format("act. policy", s -> s.isActivationPolicyUsed()).part()
                .as(s -> String.format("%s %s", s.getStartLevel(),
                        (s.isPersistentlyStarted() ? "" : "T") + (s.isActivationPolicyUsed() ? "A" : "")));

    }

    private static String bundles(Bundle[] usingBundles) {
        if (usingBundles == null) {
            return null;
        }

        return Stream.of(usingBundles).map(b -> b.getBundleId() + "").collect(Collectors.joining("\n"));
    }

    private static long getServiceId(ServiceReference<?> s) {
        return (Long) s.getProperty(Constants.SERVICE_ID);
    }

    static String objectClass(ServiceReference<?> ref) {
        return DisplayUtil.objectClass(DisplayUtil.toMap(ref));
    }

    private static String state(Bundle b) {

        switch (b.getState()) {
        case Bundle.ACTIVE:
            return "ACTV";
        case Bundle.INSTALLED:
            return "INST";
        case Bundle.RESOLVED:
            return "RSLV";
        case Bundle.STARTING:
            return "⬆︎︎";
        case Bundle.STOPPING:
            return "⬇︎︎";
        case Bundle.UNINSTALLED:
            return "UNIN";
        }
        return null;
    }

    @Descriptor(value = "delivers the FrameworkDTO")
    public FrameworkDTO frameworkDTO() {
        final var bundle = context.getBundle(0);
        final var frameworkDTO = bundle.adapt(FrameworkDTO.class);
        return frameworkDTO;
    }

    @Descriptor(value = "delivers the BundleDTOs")
    public List<BundleDTO> bundleDTO() {
        return frameworkDTO().bundles;
    }

    @Descriptor(value = "delivers the BundleDTO by id")
    public BundleDTO bundleDTO(long id) {
        return bundleDTO().stream().filter(dto -> dto.id == id).findAny()
                .orElseThrow(() -> new IllegalArgumentException(""));
    }

    @Descriptor(value = "delivers the ServiceReferenceDTOs")
    public List<ServiceReferenceDTO> serviceReferenceDTO() {
        return frameworkDTO().services;
    }

    @Descriptor(value = "delivers the ServiceReferenceDTO by id")
    public ServiceReferenceDTO serviceReferenceDTO(long id) {
        return serviceReferenceDTO().stream().filter(dto -> dto.id == id).findAny()
                .orElseThrow(() -> new IllegalArgumentException(""));
    }

    @Descriptor("Show the Bundle Symbolic Name")
    public String bsn(Bundle b) {
        return b.getSymbolicName();
    }

    /**
     * Services
     */
    @Descriptor("shows the services")
    public List<ServiceReference<?>> srv(
            @Descriptor("Registering bundle") @Parameter(absentValue = "0", names = { "-b", "--bundle" }) Bundle owner)
            throws InvalidSyntaxException {
        return srv(owner, GlobFilter.ALL);
    }

    @Descriptor("shows the services and filter")
    public List<ServiceReference<?>> srv(
            @Descriptor("Registering bundle") @Parameter(absentValue = "0", names = { "-b", "--bundle" }) Bundle owner,
            @Descriptor("Filter") GlobFilter glob) throws InvalidSyntaxException {

        var filter = "(objectClass=*" + glob + "*)";
        var refs = context.getAllServiceReferences((String) null, filter);

        var bundleId = owner.getBundleId();
        if (refs == null) {
            return Collections.emptyList();
        }

        return Stream.of(refs).filter(ref -> {
            return bundleId == 0L || bundleId == ref.getBundle().getBundleId();
        }).collect(Collectors.toList());
    }

    @Descriptor("shows the service")
    public ServiceReference<?> srv(int id) throws InvalidSyntaxException {
        var allServiceReferences = context.getAllServiceReferences((String) null, "(service.id=" + id + ")");
        if (allServiceReferences == null) {
            return null;
        }
        assert allServiceReferences.length == 1;
        return allServiceReferences[0];
    }

    /**
     * Startlevel
     */
    @Descriptor("query the bundle start level")
    public BundleStartLevel startlevel(@Descriptor("bundle to query") Bundle bundle) {
        return _startlevel(bundle);
    }

    public static BundleStartLevel _startlevel(@Descriptor("bundle to query") Bundle bundle) {
        var startlevel = bundle.adapt(BundleStartLevel.class);
        if (startlevel == null) {
            return null;
        }
        return startlevel;
    }

    enum Sort {
        id, bsn, level, time
    }

    public static List<Bundle> lb(BundleContext bc, boolean notactive, Sort sort, boolean descending,
            GlobFilter... matches) {

        Comparator<Bundle> cmp;
        if (sort == null) {
            sort = Sort.id;
        }

        cmp = switch (sort) {
        case id -> (a, b) -> Long.compare(a.getBundleId(), b.getBundleId());
        default -> (a, b) -> Long.compare(a.getBundleId(), b.getBundleId());
        case bsn -> (a, b) -> a.getSymbolicName().compareTo(b.getSymbolicName());
        case level -> (a, b) -> Integer.compare(_startlevel(a).getStartLevel(), _startlevel(b).getStartLevel());
        case time -> (a, b) -> Long.compare(a.getLastModified(), b.getLastModified());
        };
        if (descending) {
            var old = cmp;
            cmp = (a, b) -> old.compare(b, a);
        }

        return Arrays.asList(bc.getBundles()).stream().filter(k -> !notactive || in(k.getState(), ~Bundle.ACTIVE))
                .sorted(cmp).filter(k -> any(matches, k.getSymbolicName())).collect(Collectors.toList());

    }

    @Descriptor("List all current bundles")
    public List<Bundle> lb(
            @Descriptor("show only the not active bundles") @Parameter(absentValue = "false", presentValue = "true", names = {
                    "-n", "--notactive" }) boolean notactive,
            @Descriptor("sort by: id | bsn | time | level. Default is an ascending sort") @Parameter(absentValue = "id", names = {
                    "-s", "--sort" }) Sort sort,
            @Descriptor("sort in descending order (the default is ascending)") @Parameter(absentValue = "false", presentValue = "true", names = {
                    "-d", "--descending" }) boolean descending,
            GlobFilter... matches) {
        return lb(context, notactive, sort, descending, matches);
    }

    private static boolean in(int state, int... s) {
        for (int x : s) {
            if ((x & state) != 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean any(GlobFilter[] matches, String symbolicName) {
        if (matches == null || matches.length == 0) {
            return true;
        }

        for (GlobFilter g : matches) {
            if (g.matches(symbolicName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Headers
     */
    @Descriptor("display bundle headers")
    public Map<Bundle, Map<String, String>> headers(
            @Descriptor("header name, can be globbed") @Parameter(absentValue = "*", names = { "-h",
                    "--header" }) String header,
            @Descriptor("filter on value, can use globbing") @Parameter(absentValue = "*", names = { "-v",
                    "--value" }) String filter,
            @Descriptor("target bundles, if none specified all bundles are used") Bundle... bundles) {
        bundles = ((bundles == null) || (bundles.length == 0)) ? context.getBundles() : bundles;

        var hp = new GlobFilter(header);
        var vp = new GlobFilter(filter);

        Map<Bundle, Map<String, String>> result = new HashMap<>();

        for (Bundle bundle : bundles) {

            Map<String, String> headers = new TreeMap<>();

            var dict = bundle.getHeaders();
            var keys = dict.keys();
            while (keys.hasMoreElements()) {
                var k = keys.nextElement();
                var v = dict.get(k);
                if (hp.createMatcher(k).find() && vp.createMatcher(v).find()) {
                    headers.put(k, v);
                }
            }
            if (headers.size() > 0) {
                result.put(bundle, headers);
            }
        }

        return result;
    }

    @Descriptor("determines the class loader for a class name and a bundle")
    public ClassLoader which(@Descriptor("the bundle to load the class from") Bundle bundle,
            @Descriptor("the name of the class to load from bundle") String className) throws ClassNotFoundException {
        Objects.requireNonNull(bundle);
        Objects.requireNonNull(className);

        return bundle.loadClass(className).getClassLoader();
    }

    @Descriptor("query the framework start level")
    public FrameworkStartLevel startlevel() {
        return startlevel(context);
    }

    public static FrameworkStartLevel startlevel(BundleContext bc) {
        return bc.getBundle(0L).adapt(FrameworkStartLevel.class);
    }
}
