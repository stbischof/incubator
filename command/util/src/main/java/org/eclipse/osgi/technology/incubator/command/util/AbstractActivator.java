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
package org.eclipse.osgi.technology.incubator.command.util;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.Converter;
import org.apache.felix.service.command.Descriptor;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

@org.osgi.annotation.bundle.Capability(namespace = "org.apache.felix.gogo", name = "command.implementation", version = "1.0.0")
@org.osgi.annotation.bundle.Requirement(effective = "active", namespace = "org.apache.felix.gogo", name = "runtime.implementation", version = "1.0.0")
public abstract class AbstractActivator implements BundleActivator {

    protected final Set<Closeable> closeables = new HashSet<>();
    protected BundleContext context;
    protected DTOFormatter formatter = new DTOFormatter();

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        init();
    }

    protected abstract void init();

    protected void registerConverterService(Converter converterService) {
        registerConverterService(converterService, null);

    }

    protected void registerConverterService(Converter converterService, Map<String, Object> properties) {
        var minimalProperties = new Hashtable<String, Object>();
        minimalProperties.put(Constants.SERVICE_RANKING, 10000);

        if (properties != null) {
            minimalProperties.putAll(properties);
        }

        ServiceRegistration<Converter> registration = context.registerService(Converter.class, converterService,
                minimalProperties);
        closeables.add(() -> {
            registration.unregister();
        });
    }

    protected void registerCommandService(Object service, String scope) throws Exception {
        registerCommandService(service, scope, null);
    }

    protected void registerCommandService(Object service, String scope, Map<String, Object> properties) {
        try {

            var minimalProperties = new Hashtable<String, Object>();
            minimalProperties.put(CommandProcessor.COMMAND_SCOPE, scope);
            if (properties != null) {
                minimalProperties.putAll(properties);
            }

            Set<String> commands = new TreeSet<>();
            for (Method m : service.getClass().getMethods()) {
                var d = m.getAnnotation(Descriptor.class);

                if (d != null) {
                    commands.add(m.getName().toLowerCase());
                }
            }

            var functions = commands.stream().map(name -> name.startsWith("_") ? name.substring(1) : name)
                    .toArray(String[]::new);

            minimalProperties.put(CommandProcessor.COMMAND_FUNCTION, functions);

            ServiceRegistration<Object> registration = context.registerService(Object.class, service,
                    minimalProperties);

            closeables.add(() -> {
                registration.unregister();
                if (service instanceof Closeable) {
                    ((Closeable) service).close();
                }
            });
        } catch (Throwable e) {
            // ignore
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        closeables.forEach(c -> {
            try {
                c.close();
            } catch (Exception e) {
                // ignore
            }
        });
    }

}
