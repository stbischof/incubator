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

import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.service.command.Converter;
import org.osgi.framework.wiring.BundleRevision;

public class Search implements Converter {
    public String serviceName;
    public BundleRevision searcher;
    public Set<Long> matched = new HashSet<>();
    public Set<Long> mismatched = new HashSet<>();

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(getState());

        sb.append("[").append(searcher.getBundle().getBundleId()).append("] ");
        sb.append(serviceName);

        sb.append(" ").append(matched);
        if (!mismatched.isEmpty()) {
            sb.append(" !! ").append(mismatched);
        }
        return sb.toString();
    }

    private String getState() {
        if (!mismatched.isEmpty()) {
            return "! ";
        } else if (matched.isEmpty()) {
            return "? ";
        } else {
            return "  ";
        }
    }

    @Override
    public Object convert(Class<?> targetType, Object source) throws Exception {
        return null;
    }

    @Override
    public CharSequence format(Object source, int level, Converter next) throws Exception {
        switch (level) {
        case Converter.INSPECT:
            return inspect(this, next);

        case Converter.LINE:
            return line(this, next);

        case Converter.PART:
            return part(this, next);
        }
        return null;
    }

    private CharSequence part(Search search, Converter next) {
        return toString();
    }

    private CharSequence line(Search search, Converter next) throws Exception {
        try (var f = new Formatter()) {
            f.format("%s %-60s %-50s %s %s", getState(), search.serviceName, search.searcher.getBundle(),
                    search.matched.isEmpty() ? "" : search.matched,
                    search.mismatched.isEmpty() ? "" : "!! " + search.mismatched);
            return f.toString();
        }
    }

    private CharSequence inspect(Search search, Converter next) throws Exception {
        var context = search.searcher.getBundle().getBundleContext();

        try (var f = new Formatter()) {
            f.format("Searching Bundle                %s\n", search.searcher.getBundle());
            f.format("Service Name                    %s\n", search.serviceName);
            if (!search.matched.isEmpty()) {
                f.format("Registrars in same class space \n");
                for (Long b : search.matched) {
                    var bundle = context.getBundle(b);
                    f.format("  %s\n", next.format(bundle, Converter.LINE, next));
                }
            }

            if (!search.mismatched.isEmpty()) {
                f.format("!!! Registrars in different class space \n");
                for (Long b : search.mismatched) {
                    var bundle = context.getBundle(b);
                    f.format("  %s\n", next.format(bundle, Converter.LINE, next));
                }
            }
            return f.toString();
        }
    }

}
