package org.eclipse.osgi.technology.incubator.command.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.Canvas;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.Table;

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
public class TableTest {

    @org.junit.jupiter.api.Test
    public void testTableWithEmptyValue() {
        Table table = new Table(1, 0, 0);
        int w = table.width();
        int h = table.height();
        table.render();

    }

    @org.junit.jupiter.api.Test
    public void testUnfilledTable() {
        Table t = new Table(1, 1, 0);
        assertEquals("" + "┌─┐\n" + "│ │\n" + "└─┘\n" + "", t.toString());

    }

    @org.junit.jupiter.api.Test
    public void testEmptyTable() {
        Table t = new Table(0, 0, 0);
        assertEquals("" + "☒" + "", t.toString());

    }

    @org.junit.jupiter.api.Test
    public void testSimpleTable() {
        Table t = new Table(2, 2, 0);
        t.set(0, 0, "0x0");
        t.set(0, 1, "0x1");
        t.set(1, 0, "1x0");
        t.set(1, 1, "1x1");
        assertEquals("" + "┌───┬───┐\n" + "│0x0│0x1│\n" + "├───┼───┤\n" + "│1x0│1x1│\n" + "└───┴───┘\n" + "",
                t.toString());
    }

    @org.junit.jupiter.api.Test
    public void testTableWithUnequalColumns() {
        Table t = new Table(2, 2, 0);
        t.set(0, 0, "0x0xxxx");
        t.set(0, 1, "0x1");
        t.set(1, 0, "1x0");
        t.set(1, 1, "1x1yyyyyyyyyyyyyyyyyyy");
        assertEquals("" + "┌───────┬──────────────────────┐\n" + "│0x0xxxx│0x1                   │\n"
                + "├───────┼──────────────────────┤\n" + "│1x0    │1x1yyyyyyyyyyyyyyyyyyy│\n"
                + "└───────┴──────────────────────┘\n" + "", t.toString());
    }

    @org.junit.jupiter.api.Test
    public void testTableWithUnequalRows() {
        Table t = new Table(2, 2, 0);
        t.set(0, 0, "0x0\n0x0");
        t.set(0, 1, "0x1");
        t.set(1, 0, "1x0");
        t.set(1, 1, "1x1");
        assertEquals(
                "" + "┌───┬───┐\n" + "│0x0│0x1│\n" + "│0x0│   │\n" + "├───┼───┤\n" + "│1x0│1x1│\n" + "└───┴───┘\n" + "",
                t.toString());
    }

    @org.junit.jupiter.api.Test
    public void testNestedTable() {
        Table t1 = new Table(2, 2, 0);
        t1.set(0, 0, "0x0/1");
        t1.set(0, 1, "0x1/1");
        t1.set(1, 1, "1x1/1");

        Table t2 = new Table(2, 2, 0);
        t2.set(0, 1, "0x1/2");
        t2.set(1, 0, "1x0/2");
        t2.set(1, 1, "1x1/2");

        Table t3 = new Table(2, 2, 0);
        t3.set(0, 0, "0x0/3");
        t3.set(0, 1, "0x1/3");
        t3.set(1, 0, "1x0/3");
        t3.set(1, 1, "1x1/3");

        t1.set(1, 0, t2);
        t2.set(0, 0, t3);

        assertEquals("" + "┌─────────────────┬─────┐\n" + "│0x0/1            │0x1/1│\n" + "├─────┬─────┬─────┼─────┤\n"
                + "│0x0/3│0x1/3│0x1/2│1x1/1│\n" + "├─────┼─────┤     │     │\n" + "│1x0/3│1x1/3│     │     │\n"
                + "├─────┴─────┼─────┤     │\n" + "│1x0/2      │1x1/2│     │\n" + "└───────────┴─────┴─────┘\n" + "",
                t1.toString());

        Canvas render = t3.render();
        Canvas rb = render.removeBoxes();
        System.out.println(t3);
        System.out.println(rb);
    }

    @org.junit.jupiter.api.Test
    public void testNestedTableWithTopBold() {
        Table t1 = new Table(2, 2, 0);
        t1.setBold();
        t1.set(0, 0, "0x0/1");
        t1.set(0, 1, "0x1/1");
        t1.set(1, 1, "1x1/1");

        Table t2 = new Table(2, 2, 0);
        t2.set(0, 1, "0x1/2");
        t2.set(1, 0, "1x0/2");
        t2.set(1, 1, "1x1/2");

        Table t3 = new Table(2, 2, 0);
        t3.set(0, 0, "0x0/3");
        t3.set(0, 1, "0x1/3");
        t3.set(1, 0, "1x0/3");
        t3.set(1, 1, "1x1/3");

        t1.set(1, 0, t2);
        t2.set(0, 0, t3);

        assertEquals("" + "┏━━━━━━━━━━━━━━━━━┯━━━━━┓\n" + "┃0x0/1            │0x1/1┃\n" + "┠─────┬─────┬─────┼─────┨\n"
                + "┃0x0/3│0x1/3│0x1/2│1x1/1┃\n" + "┠─────┼─────┤     │     ┃\n" + "┃1x0/3│1x1/3│     │     ┃\n"
                + "┠─────┴─────┼─────┤     ┃\n" + "┃1x0/2      │1x1/2│     ┃\n" + "┗━━━━━━━━━━━┷━━━━━┷━━━━━┛\n" + "",
                t1.toString());

        System.out.println(t3);
        System.out.println(t3.render().removeBoxes());
    }
}