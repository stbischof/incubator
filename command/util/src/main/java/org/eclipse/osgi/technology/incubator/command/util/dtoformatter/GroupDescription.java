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
package org.eclipse.osgi.technology.incubator.command.util.dtoformatter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class GroupDescription {
    String title;
    final Map<String, ItemDescription> items = new LinkedHashMap<>();
    String separator = ",";
    String prefix = "[";
    String suffix = "]";
    public Function<Object, String> format;
}
