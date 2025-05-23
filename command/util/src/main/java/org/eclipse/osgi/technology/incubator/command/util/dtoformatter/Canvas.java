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

import java.util.Arrays;

public class Canvas {
    // n
    // |
    // w - + - e
    // |
    // s

    final static char PRIVATE = 0xE000;
    final static int north = PRIVATE + 0b0000_0001;
    final static int bnorth = PRIVATE + 0b0000_0011;

    final static int east = PRIVATE + 0b0000_0100;
    final static int beast = PRIVATE + 0b0000_1100;

    final static int south = PRIVATE + 0b0001_0000;
    final static int bsouth = PRIVATE + 0b0011_0000;

    final static int west = PRIVATE + 0b0100_0000;
    final static int bwest = PRIVATE + 0b1100_0000;

    static class Style {

        final int east;
        final int west;
        final int south;
        final int north;

        Style(int east, int south, int west, int north) {
            this.east = east;
            this.west = west;
            this.south = south;
            this.north = north;
        }
    }

    final static Style PLAIN = new Style(east, south, west, north);
    final static Style BOLD = new Style(beast, bsouth, bwest, bnorth);

    final static boolean[] extraline = new boolean[256];
    final static char[] boxchars = new char[256];
    static {
        box('─', west | east);
        box('━', bwest | beast);
        box('│', north | south);
        box('┃', bnorth | bsouth);
        box('┌', east | south);
        xbox('┍', beast | south);
        xbox('┎', east | bsouth);
        xbox('┏', beast | bsouth);
        box('┐', west | south);
        box('┑', bwest | south);
        box('┒', west | bsouth);
        box('┓', bwest | bsouth);
        xbox('└', north | east);
        xbox('┕', north | beast);
        xbox('┖', bnorth | east);
        xbox('┗', bnorth | beast);
        box('┘', west | north);
        box('┙', bwest | north);
        box('┚', west | bnorth);
        box('┛', bwest | bnorth);
        xbox('├', north | south | east);
        xbox('┝', north | south | beast);
        xbox('┞', bnorth | south | east);
        xbox('┟', north | bsouth | east);
        xbox('┠', bnorth | bsouth | east);
        xbox('┡', bnorth | south | beast);
        xbox('┢', north | bsouth | beast);
        xbox('┣', bnorth | bsouth | beast);
        box('┤', west | north | south);
        box('┥', bwest | north | south);
        box('┦', west | bnorth | south);
        box('┧', west | north | bsouth);
        box('┨', west | bnorth | bsouth);
        box('┩', bwest | bnorth | south);
        box('┪', bwest | north | bsouth);
        box('┫', bwest | bnorth | bsouth);
        box('┬', west | south | east);
        box('┭', bwest | south | east);
        box('┮', west | south | beast);
        box('┯', bwest | beast | south);
        box('┰', west | bsouth | east);
        box('┱', bwest | bsouth | east);
        box('┲', west | bsouth | beast);
        box('┳', bwest | bsouth | beast);
        box('┴', west | north | east);
        box('┵', bwest | north | east);
        box('┶', west | north | beast);
        box('┷', bwest | north | beast);
        box('┸', west | bnorth | east);
        box('┹', bwest | bnorth | east);
        box('┺', west | bnorth | beast);
        box('┻', bwest | bnorth | beast);
        box('┼', north | west | south | east);
        box('┽', north | bwest | south | east);
        box('┾', north | west | south | beast);
        box('┿', north | bwest | south | beast);
        box('╀', bnorth | west | south | east);
        box('╁', north | west | bsouth | east);
        box('╂', bnorth | west | bsouth | east);
        box('╃', bnorth | bwest | south | east);
        box('╄', bnorth | west | south | beast);
        box('╅', north | bwest | bsouth | east);
        box('╆', north | west | bsouth | beast);
        box('╇', bnorth | bwest | south | beast);
        box('╈', north | bwest | bsouth | beast);
        box('╉', bnorth | bwest | bsouth | east);
        box('╊', bnorth | west | bsouth | beast);
        box('╋', bnorth | bwest | bsouth | beast);
        box('╴', west);
        box('╵', north);
        box('╶', east);
        box('╷', south);
        box('╸', bwest);
        box('╹', bnorth);
        box('╺', beast);
        box('╻', bsouth);
        box('╼', west | beast);
        box('╽', north | bsouth);
        box('╾', bwest | east);
        box('╿', bnorth | south);
    }

    final static int none = 0;

    final char[] buffer;
    final int width;

    public Canvas(int width, int height) {
        this.buffer = new char[width * height];
        this.width = width;
        clear();
    }

    private static void box(char c, int i) {
        i = i - PRIVATE;
        assert boxchars[i] == 0 : "Double usage " + c + " " + i;
        assert c != 0;
        boxchars[i] = c;
    }

    private static void xbox(char c, int i) {
        box(c, i);
        i = i - PRIVATE;
        extraline[i] = true;
    }

    public Canvas clear() {
        return clear(' ');
    }

    public Canvas clear(char c) {
        Arrays.fill(buffer, c);
        return this;
    }

    /**
     * Remove boxes
     */
    public Canvas removeBoxes() {
        var w = width();
        var h = height();

        var columns = new boolean[w];
        var rows = new boolean[h];

        for (var y = 0; y < height(); y++) {
            for (var x = 0; x < width(); x++) {
                var c = get(x, y);

                if (isPrivate(c) || c == ' ') {
                    if (x == 0 || x == width() - 1) {

                    } else {
                        columns[x] = true;
                    }
                } else {
                    columns[x] = true;
                    rows[y] = true;
                }

            }
        }
        for (boolean column : columns) {
            if (!column) {
                w--;
            }
        }
        for (boolean row : rows) {
            if (!row) {
                h--;
            }
        }

        var result = new Canvas(w, h);

        var oy = 0;
        for (var y = 0; y < height(); y++) {
            if (rows[y]) {
                var ox = 0;
                for (var x = 0; x < width(); x++) {
                    if (columns[x]) {
                        var c = get(x, y);
                        if (isPrivate(c)) {
                            c = ' ';
                        }
                        result.set(ox, oy, c);
                        ox++;
                    }
                }
                oy++;
            }
        }
        return result;
    }

    public Canvas box(int x, int y, int w, int h) {
        return box(x, y, w, h, PLAIN);
    }

    public Canvas box(int x, int y, int w, int h, Style style) {
        if (w == 0 || h == 0) {
            return this;
        }
        bounds(x, y);
        w--;
        h--;
        bounds(x + w, y + h);
        line(x + 1, y, x + w - 1, y, style.east | style.west); // top horizontal
        line(x + w, y + 1, x + w, y + h - 1, style.north | style.south); // right
                                                                         // vertical
        line(x + 1, y + h, x + w - 1, y + h, style.east | style.west); // left
                                                                       // bottom
        line(x, y + 1, x, y + h - 1, style.north | style.south); // left
                                                                 // vertical
        merge(x, y, style.east | style.south);
        merge(x + w, y, style.west | style.south);
        merge(x, y + h, style.east | style.north);
        merge(x + w, y + h, style.north | style.west);
        return this;
    }

    public Canvas line(int x1, int y1, int x2, int y2, int what) {
        return line(x1, y1, x2, y2, (char) what);
    }

    public Canvas line(int x1, int y1, int x2, int y2, char what) {
        var d = 0;

        var dx = Math.abs(x2 - x1);
        var dy = Math.abs(y2 - y1);

        var dx2 = 2 * dx; // slope scaling factors to
        var dy2 = 2 * dy; // avoid floating point

        var ix = x1 < x2 ? 1 : -1; // increment direction
        var iy = y1 < y2 ? 1 : -1;

        var x = x1;
        var y = y1;

        if (dx >= dy) {
            while (true) {
                merge(x, y, what);
                if (x == x2) {
                    break;
                }
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                merge(x, y, what);
                if (y == y2) {
                    break;
                }
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }
        return this;
    }

    public Canvas merge(int x, int y, int what) {
        return merge(x, y, (char) what);
    }

    public Canvas merge(int x, int y, char what) {
        var c = get(x, y);

        if (!isPrivate(what) || !isPrivate(c)) {
            set(x, y, what);
        } else {
            set(x, y, (char) (what | c));
        }
        return this;
    }

    private boolean isPrivate(char c) {
        return c >= PRIVATE && c < PRIVATE + 256;
    }

    public char set(int x, int y, char what) {
        bounds(x, y);
        var index = y * width + x;
        var old = buffer[index];
        buffer[index] = what;
        return old;
    }

    public char get(int x, int y) {
        bounds(x, y);
        return buffer[y * width + x];
    }

    private void bounds(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException(" x < 0 " + x);
        }
        if (y < 0) {
            throw new IllegalArgumentException(" y < 0 " + y);
        }
        if (x >= width) {
            throw new IllegalArgumentException("x " + x + " is too high, max " + width);
        }
        if (y >= height()) {
            throw new IllegalArgumentException("y " + y + " is too high, max " + height());
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        if (width == 0) {
            return buffer.length;
        }
        return buffer.length / width;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        for (var y = 0; y < height(); y++) {
            var t = get(0, y);

            for (var x = 0; x < width; x++) {
                var c = get(x, y);
                if (c == 0) {
                    System.out.println("null " + x + "," + y);
                }
                if (isPrivate(c)) {
                    c = boxchar(c);
                }
                sb.append(c);
            }
            sb.append("\n");
        }
    }

    char boxchar(char c) {
        return boxchars[c - PRIVATE];
    }

    public Canvas box() {
        box(0, 0, width(), height(), PLAIN);
        return this;
    }

    public Canvas box(Style style) {
        box(0, 0, width(), height(), style);
        return this;
    }

    public Canvas merge(Canvas secondary, int dx, int dy) {
        for (var y = 0; y < secondary.height() && dy + y < height(); y++) {
            for (var x = 0; x < secondary.width() && dx + x < width(); x++) {
                if (dy + y >= 0 && dx + x >= 0) {
                    var c = secondary.get(x, y);
                    merge(dx + x, dy + y, c);
                }
            }
        }
        return this;
    }

    public void set(int x, int y, String message) {
        for (var i = 0; i < message.length(); i++) {
            if (x + i < width() && y < height()) {
                set(x + i, y, message.charAt(i));
            }
        }

    }
}
