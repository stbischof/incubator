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

import java.io.File;
import java.nio.file.Path;

import org.apache.felix.service.command.Converter;

public class FileConverter implements Converter {


    public FileConverter() {
    }

    @Override
    public Object convert(Class<?> desiredType, Object sourceObject) throws Exception {
        if (desiredType == File.class && sourceObject instanceof String s) {
            return Path.of(s).toFile();
        }
        if (desiredType == Path.class && sourceObject instanceof String s) {
            return Path.of(s);
        }
        if (desiredType == Path.class && sourceObject instanceof File f) {
            return f.toPath();
        }
        if (desiredType == File.class && sourceObject instanceof Path p) {
            return p.toFile();
        }
        return null;
    }

    @Override
    public CharSequence format(Object target, int level, Converter escape) throws Exception {
        return null;
    }

}
