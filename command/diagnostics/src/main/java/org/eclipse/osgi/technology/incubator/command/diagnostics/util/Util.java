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

import java.util.ArrayList;
import java.util.List;

public class Util {

    static final String CWD = "_cwd";

    private final static StringBuffer m_sb = new StringBuffer();

    public static String getUnderlineString(int len) {
        synchronized (m_sb) {
            m_sb.delete(0, m_sb.length());
            for (var i = 0; i < len; i++) {
                m_sb.append('-');
            }
            return m_sb.toString();
        }
    }

    public static String getValueString(Object obj) {
        synchronized (m_sb) {
            if (obj instanceof String) {
                return (String) obj;
            } else if (obj instanceof String[] array) {
                m_sb.delete(0, m_sb.length());
                for (var i = 0; i < array.length; i++) {
                    if (i != 0) {
                        m_sb.append(", ");
                    }
                    m_sb.append(array[i]);
                }
                return m_sb.toString();
            } else if (obj instanceof Boolean) {
                return ((Boolean) obj).toString();
            } else if (obj instanceof Long) {
                return ((Long) obj).toString();
            } else if (obj instanceof Integer) {
                return ((Integer) obj).toString();
            } else if (obj instanceof Short) {
                return ((Short) obj).toString();
            } else if (obj instanceof Double) {
                return obj.toString();
            } else if (obj instanceof Float) {
                return obj.toString();
            } else if (obj == null) {
                return "null";
            } else {
                return obj.toString();
            }
        }
    }

    public static List<String> parseSubstring(String value) {
        List<String> pieces = new ArrayList<>();
        var ss = new StringBuilder();
        // int kind = SIMPLE; // assume until proven otherwise
        var wasStar = false; // indicates last piece was a star
        var leftstar = false; // track if the initial piece is a star
        var rightstar = false; // track if the final piece is a star

        var idx = 0;

        // We assume (sub)strings can contain leading and trailing blanks
        var escaped = false;
        loop: for (;;) {
            if (idx >= value.length()) {
                if (wasStar) {
                    // insert last piece as "" to handle trailing star
                    rightstar = true;
                } else {
                    pieces.add(ss.toString());
                    // accumulate the last piece
                    // note that in the case of
                    // (cn=); this might be
                    // the string "" (!=null)
                }
                ss.setLength(0);
                break loop;
            }

            // Read the next character and account for escapes.
            var c = value.charAt(idx++);
            if (!escaped && ((c == '(') || (c == ')'))) {
                throw new IllegalArgumentException("Illegal value: " + value);
            } else if (!escaped && (c == '*')) {
                if (wasStar) {
                    // encountered two successive stars;
                    // I assume this is illegal
                    throw new IllegalArgumentException("Invalid filter string: " + value);
                }
                if (ss.length() > 0) {
                    pieces.add(ss.toString()); // accumulate the pieces
                    // between '*' occurrences
                }
                ss.setLength(0);
                // if this is a leading star, then track it
                if (pieces.isEmpty()) {
                    leftstar = true;
                }
                wasStar = true;
            } else if (!escaped && (c == '\\')) {
                escaped = true;
            } else {
                escaped = false;
                wasStar = false;
                ss.append(c);
            }
        }
        if (leftstar || rightstar || pieces.size() > 1) {
            // insert leading and/or trailing "" to anchor ends
            if (rightstar) {
                pieces.add("");
            }
            if (leftstar) {
                pieces.add(0, "");
            }
        }
        return pieces;
    }

    public static String unparseSubstring(List<String> pieces) {
        var sb = new StringBuilder();
        for (var i = 0; i < pieces.size(); i++) {
            if (i > 0) {
                sb.append("*");
            }
            sb.append(pieces.get(i));
        }
        return sb.toString();
    }

    public static boolean compareSubstring(List<String> pieces, String s) {
        // Walk the pieces to match the string
        // There are implicit stars between each piece,
        // and the first and last pieces might be "" to anchor the match.
        // assert (pieces.length > 1)
        // minimal case is <string>*<string>

        var len = pieces.size();

        // Special case, if there is only one piece, then
        // we must perform an equality test.
        if (len == 1) {
            return s.equals(pieces.get(0));
        }

        // Otherwise, check whether the pieces match
        // the specified string.

        var index = 0;

        for (var i = 0; i < len; i++) {
            var piece = pieces.get(i);

            // If this is the first piece, then make sure the
            // string starts with it.
            if (i == 0) {
                if (!s.startsWith(piece)) {
                    return false;
                }
            }

            // If this is the last piece, then make sure the
            // string ends with it.
            if (i == len - 1) {
                return s.endsWith(piece);
            }

            // If this is neither the first or last piece, then
            // make sure the string contains it.
            if (i > 0) {
                index = s.indexOf(piece, index);
                if (index < 0) {
                    return false;
                }
            }

            // Move string index beyond the matching piece.
            index += piece.length();
        }

        return true;
    }

}
