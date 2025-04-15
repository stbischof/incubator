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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.osgi.technology.incubator.command.diagnostics.util.Util;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

public class InspectCommand {
    public static final String NONSTANDARD_SERVICE_NAMESPACE = "service";

    public static final String CAPABILITY = "capability";
    public static final String REQUIREMENT = "requirement";

    private static final String EMPTY_MESSAGE = "[EMPTY]";
    private static final String UNUSED_MESSAGE = "[UNUSED]";
    private static final String UNRESOLVED_MESSAGE = "[UNRESOLVED]";

    private final BundleContext m_bc;

    public InspectCommand(BundleContext bc, DTOFormatter formatter) {
        m_bc = bc;
    }

    @Descriptor("inspects bundle capabilities and requirements")
    public void inspect(@Descriptor("('capability' | 'requirement')") String direction,
            @Descriptor("(<namespace> | 'service')") String namespace, @Descriptor("target bundles") Bundle[] bundles) {
        inspect(m_bc, direction, namespace, bundles);
    }

    private static void inspect(BundleContext bc, String direction, String namespace, Bundle[] bundles) {
        // Verify arguments.
        if (isValidDirection(direction)) {
            bundles = ((bundles == null) || (bundles.length == 0)) ? bc.getBundles() : bundles;

            if (CAPABILITY.startsWith(direction)) {
                printCapabilities(bc, Util.parseSubstring(namespace), bundles);
            } else {
                printRequirements(bc, Util.parseSubstring(namespace), bundles);
            }
        } else {
            if (!isValidDirection(direction)) {
                System.out.println("Invalid argument: " + direction);
            }
        }
    }

    public static void printCapabilities(BundleContext bc, List<String> namespace, Bundle[] bundles) {
        var separatorNeeded = false;
        for (Bundle b : bundles) {
            if (separatorNeeded) {
                System.out.println();
            }

            // Print out any matching generic capabilities.
            var wiring = b.adapt(BundleWiring.class);
            if (wiring != null) {
                var title = b + " provides:";
                System.out.println(title);
                System.out.println(Util.getUnderlineString(title.length()));

                // Print generic capabilities for matching namespaces.
                var matches = printMatchingCapabilities(wiring, namespace);

                // Handle service capabilities separately, since they aren't
                // part
                // of the generic model in OSGi.
                if (matchNamespace(namespace, NONSTANDARD_SERVICE_NAMESPACE)) {
                    matches |= printServiceCapabilities(b);
                }

                // If there were no capabilities for the specified namespace,
                // then say so.
                if (!matches) {
                    System.out.println(Util.unparseSubstring(namespace) + " " + EMPTY_MESSAGE);
                }
            } else {
                System.out.println("Bundle " + b.getBundleId() + " is not resolved.");
            }
            separatorNeeded = true;
        }
    }

    private static boolean printMatchingCapabilities(BundleWiring wiring, List<String> namespace) {
        var wires = wiring.getProvidedWires(null);
        var aggregateCaps = aggregateCapabilities(namespace, wires);
        var allCaps = wiring.getCapabilities(null);
        var matches = false;
        for (BundleCapability cap : allCaps) {
            if (matchNamespace(namespace, cap.getNamespace())) {
                matches = true;
                var dependents = aggregateCaps.get(cap);
                var keyAttr = cap.getAttributes().get(cap.getNamespace());
                if (dependents != null) {
                    String msg;
                    if (keyAttr != null) {
                        msg = cap.getNamespace() + "; " + keyAttr + " " + getVersionFromCapability(cap);
                    } else {
                        msg = cap.toString();
                    }
                    msg = msg + " required by:";
                    System.out.println(msg);
                    for (BundleWire wire : dependents) {
                        System.out.println("   " + wire.getRequirerWiring().getBundle());
                    }
                } else if (keyAttr != null) {
                    System.out.println(cap.getNamespace() + "; " + cap.getAttributes().get(cap.getNamespace()) + " "
                            + getVersionFromCapability(cap) + " " + UNUSED_MESSAGE);
                } else {
                    System.out.println(cap + " " + UNUSED_MESSAGE);
                }
            }
        }
        return matches;
    }

    private static Map<BundleCapability, List<BundleWire>> aggregateCapabilities(List<String> namespace,
            List<BundleWire> wires) {
        // Aggregate matching capabilities.
        Map<BundleCapability, List<BundleWire>> map = new HashMap<>();
        for (BundleWire wire : wires) {
            if (matchNamespace(namespace, wire.getCapability().getNamespace())) {
                var dependents = map.get(wire.getCapability());
                if (dependents == null) {
                    dependents = new ArrayList<>();
                    map.put(wire.getCapability(), dependents);
                }
                dependents.add(wire);
            }
        }
        return map;
    }

    static boolean printServiceCapabilities(Bundle b) {
        var matches = false;

        try {
            var refs = b.getRegisteredServices();

            if ((refs != null) && (refs.length > 0)) {
                matches = true;
                // Print properties for each service.
                for (ServiceReference<?> ref : refs) {
                    // Print object class with "namespace".
                    System.out.println(NONSTANDARD_SERVICE_NAMESPACE + "; "
                            + Util.getValueString(ref.getProperty("objectClass")) + " with properties:");
                    // Print service properties.
                    var keys = ref.getPropertyKeys();
                    for (String key : keys) {
                        if (!key.equalsIgnoreCase(Constants.OBJECTCLASS)) {
                            var v = ref.getProperty(key);
                            System.out.println("   " + key + " = " + Util.getValueString(v));
                        }
                    }
                    var users = ref.getUsingBundles();
                    if ((users != null) && (users.length > 0)) {
                        System.out.println("   Used by:");
                        for (Bundle user : users) {
                            System.out.println("      " + user);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }

        return matches;
    }

    public static void printRequirements(BundleContext bc, List<String> namespace, Bundle[] bundles) {
        var separatorNeeded = false;
        for (Bundle b : bundles) {
            if (separatorNeeded) {
                System.out.println();
            }

            // Print out any matching generic requirements.
            var wiring = b.adapt(BundleWiring.class);
            if (wiring != null) {
                var title = b + " requires:";
                System.out.println(title);
                System.out.println(Util.getUnderlineString(title.length()));
                var matches = printMatchingRequirements(wiring, namespace);

                // Handle service requirements separately, since they aren't
                // part
                // of the generic model in OSGi.
                if (matchNamespace(namespace, NONSTANDARD_SERVICE_NAMESPACE)) {
                    matches |= printServiceRequirements(b);
                }

                // If there were no requirements for the specified namespace,
                // then say so.
                if (!matches) {
                    System.out.println(Util.unparseSubstring(namespace) + " " + EMPTY_MESSAGE);
                }
            } else {
                System.out.println("Bundle " + b.getBundleId() + " is not resolved.");
            }

            separatorNeeded = true;
        }
    }

    private static boolean printMatchingRequirements(BundleWiring wiring, List<String> namespace) {
        var wires = wiring.getRequiredWires(null);
        var aggregateReqs = aggregateRequirements(namespace, wires);
        var allReqs = wiring.getRequirements(null);
        var matches = false;
        for (BundleRequirement req : allReqs) {
            if (matchNamespace(namespace, req.getNamespace())) {
                matches = true;
                var providers = aggregateReqs.get(req);
                if (providers != null) {
                    System.out.println(req.getNamespace() + "; " + req.getDirectives().get(Constants.FILTER_DIRECTIVE)
                            + " resolved by:");
                    for (BundleWire wire : providers) {
                        String msg;
                        var keyAttr = wire.getCapability().getAttributes().get(wire.getCapability().getNamespace());
                        if (keyAttr != null) {
                            msg = wire.getCapability().getNamespace() + "; " + keyAttr + " "
                                    + getVersionFromCapability(wire.getCapability());
                        } else {
                            msg = wire.getCapability().toString();
                        }
                        msg = "   " + msg + " from " + wire.getProviderWiring().getBundle();
                        System.out.println(msg);
                    }
                } else {
                    System.out.println(req.getNamespace() + "; " + req.getDirectives().get(Constants.FILTER_DIRECTIVE)
                            + " " + UNRESOLVED_MESSAGE);
                }
            }
        }
        return matches;
    }

    private static Map<BundleRequirement, List<BundleWire>> aggregateRequirements(List<String> namespace,
            List<BundleWire> wires) {
        // Aggregate matching capabilities.
        Map<BundleRequirement, List<BundleWire>> map = new HashMap<>();
        for (BundleWire wire : wires) {
            if (matchNamespace(namespace, wire.getRequirement().getNamespace())) {
                var providers = map.get(wire.getRequirement());
                if (providers == null) {
                    providers = new ArrayList<>();
                    map.put(wire.getRequirement(), providers);
                }
                providers.add(wire);
            }
        }
        return map;
    }

    static boolean printServiceRequirements(Bundle b) {
        var matches = false;

        try {
            var refs = b.getServicesInUse();

            if ((refs != null) && (refs.length > 0)) {
                matches = true;
                // Print properties for each service.
                for (ServiceReference<?> ref : refs) {
                    // Print object class with "namespace".
                    System.out.println(NONSTANDARD_SERVICE_NAMESPACE + "; "
                            + Util.getValueString(ref.getProperty("objectClass")) + " provided by:");
                    System.out.println("   " + ref.getBundle());
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }

        return matches;
    }

    private static String getVersionFromCapability(BundleCapability c) {
        var o = c.getAttributes().get(Constants.VERSION_ATTRIBUTE);
        if (o == null) {
            o = c.getAttributes().get(Constants.BUNDLE_VERSION_ATTRIBUTE);
        }
        return (o == null) ? "" : o.toString();
    }

    private static boolean matchNamespace(List<String> namespace, String actual) {
        return Util.compareSubstring(namespace, actual);
    }

    private static boolean isValidDirection(String direction) {
        return (CAPABILITY.startsWith(direction) || REQUIREMENT.startsWith(direction));
    }

}
