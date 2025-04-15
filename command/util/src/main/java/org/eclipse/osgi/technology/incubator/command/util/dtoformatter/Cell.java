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

public interface Cell {

    /**
     * Width including its borders on left and right
     */
    int width();

    /**
     * Height including its borders on top and bottom
     */
    int height();

    /**
     * Render including borders
     *
     * @param w
     * @param h
     * @return Canvas
     */
    default Canvas render(int w, int h) {
        var canvas = new Canvas(w, h);
        canvas.box();
        var lines = this.toString().split("\r?\n");
        for (var y = 0; y < lines.length && y < h - 2; y++) {
            for (var x = 0; x < w - 2 && x < lines[y].length(); x++) {
                canvas.set(x + 1, y + 1, lines[y].charAt(x));
            }
        }
        return canvas;
    }

    Object original();
}
