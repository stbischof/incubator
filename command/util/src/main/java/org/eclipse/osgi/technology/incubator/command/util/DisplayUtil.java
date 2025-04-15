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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class DisplayUtil {
    static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("kk:ss.S").withZone(ZoneId.of("UTC"));

    public static String objectClass(Map<String, Object> map) {
        var object = (String[]) map.get(Constants.OBJECTCLASS);

        return objectClass(object);
    }

    public static String objectClass(String[] object) {
        return Stream.of(object).map(DisplayUtil::shorten).collect(Collectors.joining("\n"));
    }

    public static String shorten(String className) {
        var split = className.split("\\.");
        var sb = new StringBuilder();
        sb.append(split[split.length - 1]);
        return sb.toString();

    }

    public static String dateTime(long time) {
        if (time == 0) {
            return "0";
        } else {
            return Instant.ofEpochMilli(time).toString();
        }
    }

    public static String lastModified(long time) {
        if (time == 0) {
            return "?";
        }

        var now = Instant.now();
        var modified = Instant.ofEpochMilli(time);
        var d = Duration.between(modified, now);
        var millis = d.toMillis();
        if (millis < 300_000L) {
            return (millis + 500L) / 1000L + " secs ago";
        }
        if (millis < 60L * 300_000L) {
            return (millis + 500L) / 60_000L + " mins ago";
        }
        if (millis < 60L * 60L * 300_000L) {
            return (millis + 500L) / (60L * 60_000L) + " hrs ago";
        }
        if (millis < 24L * 60L * 60L * 300_000L) {
            return (millis + 500L) / (24L * 60L * 60_000L) + " days ago";
        }
        return dateTime(time);
    }

    public static Map<String, Object> toMap(ServiceReference<?> ref) {
        Map<String, Object> map = new HashMap<>();
        for (String key : ref.getPropertyKeys()) {
            map.put(key, ref.getProperty(key));
        }
        return map;
    }

    public static String toTime(long time) {
        var timeInstant = Instant.ofEpochMilli(time);
        return dtf.format(timeInstant);
    }

}
