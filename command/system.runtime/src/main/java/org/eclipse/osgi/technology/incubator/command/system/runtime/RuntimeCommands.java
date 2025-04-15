/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation All rights
 * reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors: Stefan Bischof - initial
 */
package org.eclipse.osgi.technology.incubator.command.system.runtime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;

public class RuntimeCommands {

    public RuntimeCommands(DTOFormatter formatter) {
    }

    @Descriptor("Show the amount of free memory")
    public long freeMemory(
            @Descriptor("Run a gc first") @Parameter(absentValue = "false", presentValue = "true", names = { "-g",
                    "--gc" }) boolean gc) {
        if (gc) {
            System.gc();
        }
        return Runtime.getRuntime().freeMemory();
    }

    @Descriptor("Show memory info similar to /proc/meminfo")
    public void meminfo() {

        long max = Runtime.getRuntime().maxMemory();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;

        System.out.printf("MemMax:     %d kB\n", max / 1024);
        System.out.printf("MemTotal:     %d kB\n", total / 1024);
        System.out.printf("MemFree:      %d kB\n", free / 1024);
        System.out.printf("MemUsed:      %d kB\n", used / 1024);
    }

    @Descriptor("Show cpu info similar to /proc/cuinfo")
    public String cpuinfo() {

        StringBuilder sb = new StringBuilder();

        long processors = Runtime.getRuntime().availableProcessors();
        sb.append("ProcessorsAvailable:  " + processors + " \n");
        sb.append("Architecture:         " + System.getProperty("os.arch") + " \n");

        return sb.toString();

    }

    @Descriptor("Show cpu info similar to /proc/cuinfo")
    public String jvminfo() {
        StringBuilder sb = new StringBuilder();
        String version = Runtime.version().toString();
        sb.append("RuntimeVersion:       " + version + " \n");
        sb.append("Architecture:  " + System.getProperty("os.arch") + " \n");
        sb.append("OS Name:       " + System.getProperty("os.name") + " \n");
        sb.append("OS Version:    " + System.getProperty("os.version") + " \n");
        sb.append("Java Version:  " + System.getProperty("java.version") + " \n");
        sb.append("Java Vendor:   " + System.getProperty("java.vendor") + " \n");

        return sb.toString();

    }

    @Descriptor("showns current user")
    public String whoami() {
        return System.getProperty("user.name");
    }

    @Descriptor("Runs a gc")
    public void gc() {
        System.gc();
    }

    @Descriptor("Shows the current datetime")
    public String date() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");
        String dateOutput = LocalDateTime.now().format(formatter);
        return dateOutput;
    }


}
