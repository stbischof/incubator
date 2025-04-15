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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.Canvas;

public class CanvasTest {

    @org.junit.jupiter.api.Test
    public void testCanvas() {
        Canvas c = new Canvas(10, 10);
        c.box(0, 0, 10, 10);
        assertThat("" + "┌────────┐\n" + "│        │\n" + "│        │\n" + "│        │\n" + "│        │\n"
                + "│        │\n" + "│        │\n" + "│        │\n" + "│        │\n" + "└────────┘\n" + "")
                .isEqualTo(c.toString());
        c.box(2, 2, 6, 6);
        assertThat("" + "┌────────┐\n" + "│        │\n" + "│ ┌────┐ │\n" + "│ │    │ │\n" + "│ │    │ │\n"
                + "│ │    │ │\n" + "│ │    │ │\n" + "│ └────┘ │\n" + "│        │\n" + "└────────┘\n" + "")
                .isEqualTo(c.toString());

        c.box(0, 0, 6, 6);
        assertThat("" + "┌────┬───┐\n" + "│    │   │\n" + "│ ┌──┼─┐ │\n" + "│ │  │ │ │\n" + "│ │  │ │ │\n"
                + "├─┼──┘ │ │\n" + "│ │    │ │\n" + "│ └────┘ │\n" + "│        │\n" + "└────────┘\n" + "")
                .isEqualTo(c.toString());
        c.box(6, 6, 4, 4);
        assertThat("" + "┌────┬───┐\n" + "│    │   │\n" + "│ ┌──┼─┐ │\n" + "│ │  │ │ │\n" + "│ │  │ │ │\n"
                + "├─┼──┘ │ │\n" + "│ │   ┌┼─┤\n" + "│ └───┼┘ │\n" + "│     │  │\n" + "└─────┴──┘\n" + "")
                .isEqualTo(c.toString());
    }

    @org.junit.jupiter.api.Test
    public void testCanvasRemoveBox() {
        Canvas c = new Canvas(10, 10);
        Canvas box = c.box(0, 0, 10, 10);
        Canvas removeBoxes = box.removeBoxes();
        System.out.println(removeBoxes);
    }

    @org.junit.jupiter.api.Test
    public void testMerge() {
        Canvas c1 = new Canvas(10, 10);
        c1.box();
        Canvas c2 = new Canvas(5, 5);
        c2.box();
        c1.merge(c2, 5, 5);
        assertThat("" + "┌────────┐\n" + "│        │\n" + "│        │\n" + "│        │\n" + "│        │\n"
                + "│    ┌───┤\n" + "│    │   │\n" + "│    │   │\n" + "│    │   │\n" + "└────┴───┘\n" + "").isEqualTo(
                c1.toString());
        c1.merge(c2, 0, 0);
        c1.merge(c2, 4, 0);
        assertThat("" + "┌───┬───┬┐\n" + "│   │   ││\n" + "│   │   ││\n" + "│   │   ││\n" + "├───┴───┘│\n"
                + "│    ┌───┤\n" + "│    │   │\n" + "│    │   │\n" + "│    │   │\n" + "└────┴───┘\n" + "").isEqualTo(
                c1.toString());

        c1.clear().box();
        c1.merge(c2, -3, -2);
        assertThat("" + " ┼───────┐\n" + " │       │\n" + "┼┘       │\n" + "│        │\n" + "│        │\n"
                + "│        │\n" + "│        │\n" + "│        │\n" + "│        │\n" + "└────────┘\n" + "").isEqualTo(
                c1.toString());
        c1.merge(c2, 8, 8);
        assertThat("" + " ┼───────┐\n" + " │       │\n" + "┼┘       │\n" + "│        │\n" + "│        │\n"
                + "│        │\n" + "│        │\n" + "│        │\n" + "│       ┌┼\n" + "└───────┼ \n" + "").isEqualTo(
                c1.toString());
    }

}