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
package org.eclipse.osgi.technology.incubator.command.converter.file;

import java.util.Hashtable;

import org.apache.felix.service.command.Converter;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
@org.osgi.annotation.bundle.Capability(namespace = "org.apache.felix.gogo", name = "command.implementation", version = "1.0.0")
@org.osgi.annotation.bundle.Requirement(effective = "active", namespace = "org.apache.felix.gogo", name = "runtime.implementation", version = "1.0.0")
public class Activator implements BundleActivator {

    private ServiceRegistration<Converter> reg;

    @Override
    public void start(BundleContext context) throws Exception {

        var properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_RANKING, 10000);
        reg = context.registerService(Converter.class, new FileConverter(), properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (reg != null) {
            reg.unregister();
        }
    }
}
