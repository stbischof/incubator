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
package org.eclipse.osgi.technology.incubator.command.diagnostics.util;

import java.util.Set;
import java.util.TreeSet;

public class Export {
    public String pack;
    public Set<Long> exporters = new TreeSet<>();
    public Set<Long> privates = new TreeSet<>();

    public Export(String packageName) {
        pack = packageName;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(state()).append(" ").append(pack);

        if (!exporters.isEmpty()) {
            sb.append(" exporters=").append(exporters);
        }
        if (!privates.isEmpty()) {
            sb.append(" privates=").append(privates);
        }
        return sb.toString();
    }

    private String state() {
        return privates.isEmpty() ? " " : "!";
    }
}
