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
package org.eclipse.osgi.technology.incubator.command.osgi.service.http;

import java.util.Map;

import org.eclipse.osgi.technology.incubator.command.util.AbstractActivator;
import org.eclipse.osgi.technology.incubator.command.util.BaseDTOFormatterConverter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
@org.osgi.annotation.bundle.Capability(namespace = "org.apache.felix.gogo", name = "command.implementation", version = "1.0.0")
@org.osgi.annotation.bundle.Requirement(effective = "active", namespace = "org.apache.felix.gogo", name = "runtime.implementation", version = "1.0.0")
public class Activator extends AbstractActivator {

    @Override
    protected void init() {
        registerCommandService(new HttpWhiteboardCommands(context, formatter), "tech", Map.of());
        registerConverterService(new HttpWhiteboardConverter(formatter));
    }

    private static class HttpWhiteboardConverter extends BaseDTOFormatterConverter {

        public HttpWhiteboardConverter(DTOFormatter formatter) {
            super(formatter);
        }
    }
}
