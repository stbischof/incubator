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

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Function;

import org.apache.felix.gogo.runtime.Closure;
import org.apache.felix.service.command.Converter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;

public abstract class BaseDTOFormatterConverter implements Converter {

    private final DTOFormatter formatter;

    protected BaseDTOFormatterConverter(DTOFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public CharSequence format(Object from, int level, Converter backup) throws Exception {
        try {
            if (from instanceof Enumeration) {
                from = Collections.list((Enumeration<?>) from);
            }
            if (from instanceof File file) {
                return file.getPath();
            }
            if (from instanceof Function) {
                if (from.getClass() == Closure.class) {
                    return " { " + from.toString() + " } ";
                }
                return "a Function";
            }
            var formatted = formatter.format(from, level, (o, l, f) -> {
                try {
                    return backup.format(o, l, null);
                } catch (Exception e) {
                    return Objects.toString(o);
                }
            });

            if (formatted != null) {
                return formatted;
            }
            return formatted;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convert(Class<?> desiredType, Object in) throws Exception {
        return null;
    }
}