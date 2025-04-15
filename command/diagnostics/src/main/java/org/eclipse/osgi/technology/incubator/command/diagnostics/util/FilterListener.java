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
package org.eclipse.osgi.technology.incubator.command.diagnostics.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.ListenerHook;

public class FilterListener implements ListenerHook {
    private final static Pattern LISTENER_INFO_PATTERN = Pattern.compile("\\(objectClass=([^)]+)\\)");
    public final Map<String, List<BundleContext>> listenerContexts = new HashMap<>();

    volatile boolean quiting;
    private ServiceRegistration<ListenerHook> lhook;

    public FilterListener(BundleContext context) {
        lhook = context.registerService(ListenerHook.class, this, null);
    }

    @Override
    public synchronized void added(Collection<ListenerInfo> listeners) {
        if (quiting) {
            return;
        }
        for (Object o : listeners) {
            addListenerInfo((ListenerInfo) o);
        }
    }

    @Override
    public synchronized void removed(Collection<ListenerInfo> listeners) {
        if (quiting) {
            return;
        }
        for (Object o : listeners) {
            removeListenerInfo((ListenerInfo) o);
        }
    }

    private void addListenerInfo(ListenerInfo o) {
        var filter = o.getFilter();
        if (filter != null) {
            var m = LISTENER_INFO_PATTERN.matcher(filter);
            while (m.find()) {
                listenerContexts.compute(m.group(1), (key, list) -> {
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(o.getBundleContext());
                    return list;
                });

            }
        }
    }

    private void removeListenerInfo(ListenerInfo o) {
        var filter = o.getFilter();
        if (filter != null) {
            var m = LISTENER_INFO_PATTERN.matcher(filter);
            while (m.find()) {
                listenerContexts.computeIfPresent(m.group(1), (key, list) -> {

                    list.remove(o.getBundleContext());
                    return list;
                });
            }
        }
    }

    public void close() {
        quiting = true;
        lhook.unregister();
    }
}
