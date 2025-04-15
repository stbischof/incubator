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

import java.util.function.Function;

public class ItemDescription {
    ItemDescription(String name) {
        this.label = name;
    }

    Function<Object, Object> member;
    String label;
    int maxWidth = Integer.MAX_VALUE;
    int minWidth = 0;
    boolean self;
    Function<Object, Object> format;
}
