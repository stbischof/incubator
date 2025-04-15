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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A table consists of rows and columns of {@link Cell} objects.
 */
public class Table implements Cell {

    public final int rows;
    public final int cols;
    public final Cell[][] cells;
    final public int headers;
    Canvas.Style style = Canvas.PLAIN;
    Object original;

    public static final Cell EMPTY = new Cell() {

        @Override
        public int width() {
            return 3;
        }

        @Override
        public int height() {
            return 3;
        }

        @Override
        public String toString() {
            return " ";
        }

        @Override
        public Object original() {
            return null;
        }
    };

    public Table(int rows, int cols, int headers) {
        this.rows = rows;
        this.cols = cols;
        this.headers = headers;
        cells = new Cell[rows][cols];
        for (var r = 0; r < rows; r++) {
            for (var c = 0; c < cols; c++) {
                cells[r][c] = EMPTY;
            }
        }
    }

    public Table(List<List<String>> matrix, int headers) {
        this(matrix.size(), maxWidth(matrix), headers);
        var r = headers;

        for (List<String> row : matrix) {
            var c = 0;
            for (String col : row) {
                this.set(r, c, col);
                c++;
            }
            r++;
        }
    }

    private static int maxWidth(List<List<String>> matrix) {
        var maxWidth = 0;
        for (List<String> row : matrix) {
            maxWidth = Math.max(maxWidth, row.size());
        }
        return maxWidth;
    }

    /**
     * Width including borders
     */
    @Override
    public int width() {
        if (rows == 0 || cols == 0) {
            return 2;
        }

        var w = 0;
        for (var c = 0; c < cols; c++) {
            w += width(c) - 1; // remove right border width because we overlap
        }
        return Math.max(w, 0) + 1; // add right border
    }

    /**
     * Height including borders
     */
    @Override
    public int height() {
        if (rows == 0 || cols == 0) {
            return 2;
        }

        var h = 0;
        for (var r = 0; r < rows; r++) {
            var height = height(r);
            if (height > 0) {
                h += height - 1;// remove bottom border width because we overlap
            }
        }
        return Math.max(h, 0) + 1; // add right border
    }

    public void set(int r, int c, Object label) {
        cells[r][c] = new StringCell("" + label, label);
    }

    public void set(int r, int c, Cell table) {
        if (table == null || table.width() == 0 || table.height() == 0) {
            table = Table.EMPTY;
        }

        cells[r][c] = table;
    }

    public Cell[] row(int row) {
        return cells[row];
    }

    @Override
    public String toString() {
        return toString(null);
    }

    @Override
    public Canvas render(int width, int height) {
        return render(width, height, 0, 0, 0, 0);
    }

    public Canvas render() {
        return render(width(), height(), 0, 0, 0, 0);
    }

    public Canvas render(int width, int height, int left, int top, int right, int bottom) {

        var canvas = new Canvas(width + left + right, height + top + bottom);
        canvas.box(left, top, width, height, style);

        var y = top;

        for (var r = 0; r < rows; r++) {
            var ch = height(r);
            var x = left;
            for (var c = 0; c < cols; c++) {
                int cw;
                if (c == cols - 1) {
                    // adjust last column width
                    cw = width - x - left;
                } else {
                    cw = width(c);
                }
                var cell = cells[r][c];
                var foo = cell.render(cw, ch);
                canvas.merge(foo, x, y);
                x += cw - 1;
            }
            y += ch - 1;
        }
        return canvas;
    }

    /**
     * Width of a column without borders
     *
     * @param col
     * @return
     */
    private int width(int col) {
        var w = 0;
        for (var r = 0; r < rows; r++) {
            var cell = cells[r][col];
            var width = cell.width();
            if (width > w) {
                w = width;
            }
        }
        return w;
    }

    public int height(int row) {
        var h = 0;
        for (var c = 0; c < cols; c++) {
            var cell = cells[row][c];
            var height = cell.height();
            if (height > h) {
                h = height;
            }
        }
        return h;
    }

    public Table transpose(int headers) {
        var transposed = new Table(cols, rows, headers);
        for (var row = 0; row < rows; row++) {
            for (var col = 0; col < cols; col++) {
                var c = this.get(row, col);
                transposed.set(col, row, c);
            }
        }
        return transposed;
    }

    public Cell get(int row, int col) {
        return cells[row][col];
    }

    public String toString(String message) {
        if (message == null) {
            message = "";
        }

        if (rows == 0 || cols == 0) {
            return "☒" + message;
        }
        var render = render(width(), height(), 0, 0, message.length(), 0);
        render.set(width(), 0, message);
        return render.toString();
    }

    public Table addColum(int col) {
        var t = new Table(rows, cols + 1, headers);
        if (col > 0) {
            copyTo(t, 0, 0, 0, 0, rows, col);
        }

        return copyTo(t, 0, col, 0, col + 1, rows, cols - col);
    }

    public Table setColumn(int col, Object cell) {
        for (var r = 0; r < rows; r++) {
            set(r, col, cell);
        }

        return this;
    }

    public Table copyTo(Table dest, int sourceRow, int sourceCol, int destRow, int destCol, int rows, int cols) {
        for (var i = 0; i < rows; i++) {
            for (var j = 0; j < cols; j++) {
                var cell = get(sourceRow + i, sourceCol + j);
                dest.set(destRow + i, destCol + j, cell);
            }
        }
        return dest;
    }

    public void copyColumn(Table src, int from, int to) {
        copyTo(src, 0, from, 0, to, rows, 1);
    }

    public void setBold() {
        style = Canvas.BOLD;
    }

    public Table select(List<String> columns) {
        var dst = new Table(rows, columns.size(), headers);
        for (var toColumn = 0; toColumn < columns.size(); toColumn++) {
            var srcColumn = findHeader(columns.get(toColumn));
            if (srcColumn >= 0) {
                this.copyColumn(dst, srcColumn, toColumn);
            } else {
                throw new IllegalArgumentException("No such column " + columns.get(toColumn));
            }
        }
        return dst;
    }

    /**
     * Select matching rows. Each row is translated to a map and then run against
     * the given predicate. If the predicate matches the row is included in the
     * output.
     *
     * @param predicate
     * @return a new table with only the matching rows
     */
    public Table select(Predicate<Map<String, Object>> predicate) {
        String colNames[] = new String[cols];

        if (headers == 0) {
            for (var i = 0; i < colNames.length; i++) {
                colNames[i] = "" + i;
            }
        } else {
            for (var i = 0; i < colNames.length; i++) {
                colNames[i] = get(0, i).toString();
            }
        }

        List<Integer> selectedRows = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        for (var r = headers; r < rows; r++) {
            for (var c = 0; c < cols; c++) {
                var cell = get(r, c);
                Object value;
                if (cell instanceof Table) {
                    value = ((Table) cell).toList();
                } else {
                    var s = cell.toString();
                    try {
                        value = Long.parseLong(s);
                    } catch (Exception e) {
                        try {
                            value = Double.parseDouble(s);
                        } catch (Exception ee) {
                            value = s;
                        }
                    }
                }
                map.put(colNames[c], value);
            }
            if (predicate.test(map)) {
                selectedRows.add(r);
            }
        }

        var result = new Table(selectedRows.size() + headers, cols, headers);

        copyTo(result, 0, 0, 0, 0, headers, cols);

        for (var r = 0; r < selectedRows.size(); r++) {
            copyTo(result, selectedRows.get(r), 0, r + result.headers, 0, 1, cols);
        }
        return result;
    }

    public List<String> toList() {
        List<String> list = new ArrayList<>();
        for (var r = headers; r < rows; r++) {
            list.add(toString(r));
        }
        return list;
    }

    public String toString(int row) {
        return Stream.of(cells[row]).map(Object::toString).collect(Collectors.joining(","));
    }

    public int findHeader(String name) {
        for (var h = 0; h < headers; h++) {
            for (var c = 0; c < cols; c++) {
                if (Integer.toString(c).equals(name)) {
                    return c;
                }

                var header = get(h, c).toString();
                if (header.matches(name)) {
                    return c;
                }
            }
        }
        return -1;
    }

    public void sort(String sort, boolean reverse) {
        var col = findHeader(sort);
        if (col < 0) {
            return;
        }
        Comparator<Cell[]> cmp = (var a, var b) -> {

            var aa = a[col].original();
            var bb = b[col].original();
            if (aa == bb) {
                return 0;
            }

            if (aa == null) {
                return -1;
            }
            if (bb == null) {
                return 1;
            }

            var aaa = aa.toString();
            var bbb = bb.toString();

            try {
                var la = Long.parseLong(aaa);
                var lb = Long.parseLong(bbb);
                return Long.compare(la, lb);
            } catch (Exception e) {
                try {
                    double la = Long.parseLong(aaa);
                    double lb = Long.parseLong(bbb);
                    return Double.compare(la, lb);
                } catch (Exception ee) {
                    return aaa.compareTo(bbb);
                }
            }
        };

        Arrays.sort(cells, headers, rows, reverse ? cmp.reversed() : cmp);
    }

    @Override
    public Object original() {
        return original;
    }

}
