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

public class StringCell implements Cell {
    static Justif j = new Justif(80);

    public final String[] value;
    public final int width;
    final Object original;

    public StringCell(String label, Object original) {
        this.original = original;
        this.value = label.split("\\s*\r?\n");
        var w = 0;
        for (String l : value) {
            if (l.length() > w) {
                w = l.length();
            }
        }
        this.width = w;
    }

    public StringCell(String[] array, Object original) {
        this.value = array;
        this.original = original;
        var w = 0;
        for (String l : value) {
            if (l.length() > w) {
                w = l.length();
            }
        }
        this.width = w;
    }

    @Override
    public int width() {
        return width + 2;
    }

    @Override
    public int height() {
        return value.length + 2;
    }

    @Override
    public String toString() {
        return String.join("\n", value);
    }

    @Override
    public Object original() {
        return original;
    }

}
