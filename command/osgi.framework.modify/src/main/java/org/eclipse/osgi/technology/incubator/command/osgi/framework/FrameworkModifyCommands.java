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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.FrameworkWiring;

public class FrameworkModifyCommands {

    private static final String CWD = ".cwd";

    private BundleContext context;

    public FrameworkModifyCommands(BundleContext context, DTOFormatter formatter) {
        this.context = context;
        dtos(formatter);
    }

    void dtos(DTOFormatter f) {

    }

    @Descriptor("Set the start level of bundles")
    public void startlevel(@Descriptor("startlevel, >0") int startlevel,
            @Descriptor("bundles to set. No bundles imply all bundles except the framework bundle") Bundle bundle) {

        if (bundle.getBundleId() == 0L) {
            return;
        }

        var s = bundle.adapt(BundleStartLevel.class);
        s.setStartLevel(startlevel);
    }

    enum Modifier {
        framework, initial
    }

    //@formatter:off
    @Descriptor("set either the framework or the initial bundle start level")
    public int startlevel(

        @Parameter(names = {"-w", "--wait"}, absentValue = "false", presentValue = "true")
        boolean wait,

        Modifier modifier,

        @Descriptor("either framework or initial level. If <0 then not set, currently value returned")
        int level
    ) throws InterruptedException { //@formatter:on

        var fsl = FrameworkCommands.startlevel(context);
        switch (modifier) {
        case framework: {
            var oldlevel = fsl.getStartLevel();
            if (level >= 0) {
                if (wait) {
                    var s = new Semaphore(0);
                    fsl.setStartLevel(level, e -> {
                        s.release();
                    });
                    s.acquire();
                } else {
                    fsl.setStartLevel(level);
                }
            }
            return oldlevel;
        }

        case initial: {
            var oldlevel = fsl.getInitialBundleStartLevel();
            fsl.setInitialBundleStartLevel(level);
            return oldlevel;
        }
        default:
            throw new IllegalArgumentException("invalid modifier " + modifier);
        }
    }

    @Descriptor("refresh bundles")
    //@formatter:off
    public List<Bundle> refresh(

            @Descriptor("Wait for refresh to finish before returning. The maxium time this will wait is 60 seconds. It will return the affected bundles")
            @Parameter(absentValue="false", presentValue="true", names= {"-w","--wait"})
            boolean wait,

            @Descriptor("target bundles (can be empty). If no bundles are specified then all bundles are refreshed")
            Bundle ... bundles

        // @formatter:on
    ) {
        List<Bundle> bs = Arrays.asList(bundles);

        var fw = context.getBundle(0L).adapt(FrameworkWiring.class);
        if (wait) {
            try {
                Bundle older[] = context.getBundles();
                var s = new Semaphore(0);
                fw.refreshBundles(bs, e -> {
                    if (e.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
                        s.release();
                    }
                });
                s.tryAcquire(60000, TimeUnit.MILLISECONDS);
                Bundle newer[] = context.getBundles();

                Arrays.sort(older, Comparator.comparing(Bundle::getBundleId));
                Arrays.sort(newer, Comparator.comparing(Bundle::getBundleId));
                return diff(older, newer);
            } catch (InterruptedException e1) {
                // ignore, just return
                return null;
            }
        } else {
            fw.refreshBundles(bs);
            return null;
        }
    }

    private List<Bundle> diff(Bundle[] older, Bundle[] newer) {
        List<Bundle> diffs = new ArrayList<>();
        int o = 0, n = 0;
        while (o < older.length || n < older.length) {

            if (o < older.length && n < older.length) {
                if (older[o].getBundleId() == newer[n].getBundleId()) {
                    if (older[o].getLastModified() != newer[n].getLastModified()) {
                        diffs.add(older[o]);
                    }
                    o++;
                    n++;
                } else {
                    if (older[o].getBundleId() < newer[n].getBundleId()) {
                        diffs.add(older[o]);
                        o++;
                    } else {
                        diffs.add(newer[n]);
                        n++;
                    }
                }
            } else if (o < older.length) {
                diffs.add(older[o]);
                o++;
            } else {
                diffs.add(newer[n]);
                n++;
            }
        }
        return diffs;
    }

    @Descriptor("resolve bundles")
    public List<Bundle> resolve(
            @Descriptor("to be resolved bundles. If no bundles are specified then all bundles are attempted to be resolved") Bundle... bundles) {
        List<Bundle> bs = Arrays.asList(bundles);

        var fw = context.getBundle(0L).adapt(FrameworkWiring.class);
        fw.resolveBundles(bs);
        return FrameworkCommands.lb(context, false, null, false).stream()
                .filter(b -> (b.getState() & Bundle.UNINSTALLED + Bundle.INSTALLED) != 0).collect(Collectors.toList());
    }

    @Descriptor("start bundles")
    public void start(
    //@formatter:off

        @Descriptor("start bundle transiently")
        @Parameter(names = {"-t", "--transient"}, presentValue = "true", absentValue = "false")
        boolean trans,

        @Descriptor("use declared activation policy")
        @Parameter(names = {"-p", "--policy"}, presentValue = "true", absentValue = "false")
        boolean policy,

        @Descriptor("target bundle")
        Bundle ...bundles

        //@formatter:on
    ) throws BundleException {
        var options = 0;

        // Check for "transient" switch.
        if (trans) {
            options |= Bundle.START_TRANSIENT;
        }

        // Check for "start policy" switch.
        if (policy) {
            options |= Bundle.START_ACTIVATION_POLICY;
        }

        for (Bundle bundle : bundles) {
            bundle.start(options);
        }
    }

    @Descriptor("stop bundles")
    public void stop(
    // @formatter:off
        @Parameter(names = {"-t", "--transient"}, presentValue = "true", absentValue = "false")
        @Descriptor( "stop bundle transiently")
        boolean trans,

        @Descriptor("target bundles")
        Bundle ...bundles
    // @formatter:on
    ) throws BundleException {
        var options = 0;

        if (trans) {
            options |= Bundle.STOP_TRANSIENT;
        }

        for (Bundle bundle : bundles) {
            bundle.stop(options);
        }
    }

    @Descriptor("uninstall bundles")
    public void uninstall(
    //@formatter:off

        @Descriptor("the bundles to uninstall")
        Bundle ... bundles

        // @formatter:on
    ) throws BundleException {
        for (Bundle bundle : bundles) {
            bundle.uninstall();
        }
    }

    @Descriptor("update bundle")
    public void update(
    //@formatter:off

        @Descriptor("the bundles to update")
        Bundle ... bundles

        // @formatter:on
    ) throws BundleException {
        for (Bundle b : bundles) {
            b.update();
        }
    }

    @Descriptor("update bundle from URL")
    public void update(
    // @formatter:off
        CommandSession session,

        @Descriptor("bundle to update")
        Bundle bundle,

        @Descriptor("URL from where to retrieve bundle")
        String location

    //@formatter:on
    ) throws IOException, BundleException, URISyntaxException {

        Objects.requireNonNull(bundle);
        Objects.requireNonNull(location);

        location = resolveUri(session, location.trim());
        var is = new URI(location).toURL().openStream();
        bundle.update(is);
    }

    /**
     * Intepret a string as a URI relative to the current working directory.
     *
     * @param session     the session (where the CWD is stored)
     * @param relativeUri the input URI
     * @return the resulting URI as a string
     * @throws IOException
     */
    public static String resolveUri(CommandSession session, String relativeUri) throws IOException {
        var cwd = (File) session.get(CWD);
        if (cwd == null) {
            cwd = new File("").getCanonicalFile();
            session.put(CWD, cwd);
        }
        if ((relativeUri == null) || (relativeUri.length() == 0)) {
            return relativeUri;
        }

        var curUri = cwd.toURI();
        var newUri = curUri.resolve(relativeUri);
        return newUri.toString();
    }
}
