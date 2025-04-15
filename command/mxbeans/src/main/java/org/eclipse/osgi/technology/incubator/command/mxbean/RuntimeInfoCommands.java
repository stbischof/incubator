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

package org.eclipse.osgi.technology.incubator.command.mxbean;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;

public class RuntimeInfoCommands {
    private final RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

    public RuntimeInfoCommands(DTOFormatter formatter) {
    }

    @Descriptor("Returns the name representing the running Java virtual machine")
    public String name() {
        return runtime.getName();
    }

    @Descriptor("Returns the Java specification name")
    public String specName() {
        return runtime.getSpecName();
    }

    @Descriptor("Returns the Java specification vendor")
    public String specVendor() {
        return runtime.getSpecVendor();
    }

    @Descriptor("Returns the Java specification version")
    public String specVersion() {
        return runtime.getSpecVersion();
    }

    @Descriptor("Returns the uptime of the Java virtual machine in milliseconds")
    public String uptime() {
        return runtime.getUptime() + "";
    }

    @Descriptor("Returns the start time of the Java virtual machine in milliseconds since epoch")
    public String startTime() {
        return runtime.getStartTime() + "";
    }

    @Descriptor("Returns the Java vendor")
    public String vmVendor() {
        return runtime.getVmVendor();
    }

    @Descriptor("Returns the Java VM name")
    public String vmName() {
        return runtime.getVmName();
    }

    @Descriptor("Returns the Java VM version")
    public String vmVersion() {
        return runtime.getVmVersion();
    }

    @Descriptor("Returns the process ID (pid) from the VM name")
    public String pid() {
        String name = runtime.getName(); // Format: pid@hostname
        return name.contains("@") ? name.split("@")[0] : "unknown";
    }
}