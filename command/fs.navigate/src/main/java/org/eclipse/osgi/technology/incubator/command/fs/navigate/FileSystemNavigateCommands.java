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
package org.eclipse.osgi.technology.incubator.command.fs.navigate;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.osgi.framework.BundleContext;

public class FileSystemNavigateCommands {
    private static final String CWD = ".cwd";

    public FileSystemNavigateCommands(BundleContext bc, DTOFormatter formatter) {
    }

    @Descriptor("get current directory")
    public File pwd(CommandSession session) {
        var cwd = (File) session.get(CWD);
        if (cwd == null) {
            cwd = session.currentDir().toFile();
        }
        return cwd;
    }

    @Descriptor("get current directory")
    public File cd(CommandSession session) {
        try {
            return cd(session, null);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to get current directory");
        }
    }

    @Descriptor("change current directory")
    public File cd(CommandSession session, @Descriptor("target directory") String dir) throws IOException {
        var cwd = pwd(session);
        if ((dir == null) || (dir.length() == 0)) {
            return cwd;
        }
        cwd = cwd.getAbsoluteFile().toPath().resolve(dir).toFile();

        session.put(CWD, cwd);
        return cwd;
    }

    @Descriptor("get current directory contents")
    public File[] ls(CommandSession session) throws IOException {
        return ls(session, null);
    }

    @Descriptor("get specified path contents")
    public File[] ls(CommandSession session, @Descriptor("path with optionally wildcarded file name") String pattern)
            throws IOException {
        pattern = ((pattern == null) || (pattern.length() == 0)) ? "." : pattern;
        pattern = ((pattern.charAt(0) != File.separatorChar) && (pattern.charAt(0) != '.')) ? "./" + pattern : pattern;
        var idx = pattern.lastIndexOf(File.separatorChar);
        var parent = (idx < 0) ? "." : pattern.substring(0, idx + 1);
        var target = (idx < 0) ? pattern : pattern.substring(idx + 1);

        var actualParent = ((parent.charAt(0) == File.separatorChar) ? new File(parent) : new File(cd(session), parent))
                .getCanonicalFile();

        idx = target.indexOf(File.separatorChar, idx);
        var isWildcarded = (target.indexOf('*', idx) >= 0);
        File[] files;
        if (isWildcarded) {
            if (!actualParent.exists()) {
                throw new IOException("File does not exist");
            }
            final var pieces = parseSubstring(target);
            files = actualParent.listFiles((FileFilter) pathname -> compareSubstring(pieces, pathname.getName()));
        } else {
            var actualTarget = new File(actualParent, target).getCanonicalFile();
            if (!actualTarget.exists()) {
                throw new IOException("File does not exist");
            }
            if (actualTarget.isDirectory()) {
                files = actualTarget.listFiles();
            } else {
                files = new File[] { actualTarget };
            }
        }
        return files;
    }

    @Descriptor("get tree under current directory")
    public String tree(CommandSession session) {
        var cwd = (File) session.get(CWD);
        if (cwd == null) {
            cwd = session.currentDir().toFile();
        }

        StringBuilder sb = new StringBuilder();
        printTree(sb, cwd, "");
        return sb.toString();
    }

    private static List<String> parseSubstring(String value) {
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

    private static boolean compareSubstring(List<String> pieces, String s) {
        // Walk the pieces to match the string
        // There are implicit stars between each piece,
        // and the first and last pieces might be "" to anchor the match.
        // assert (pieces.length > 1)
        // minimal case is <string>*<string>

        var len = pieces.size();

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


    private static void printTree(StringBuilder sb, File folder, String prefix) {
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;

        Arrays.sort(files);

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            boolean isLast = (i == files.length - 1);
            sb.append(prefix)
              .append(isLast ? "└── " : "├── ")
              .append(file.getName());

            if (file.isDirectory()) {
                sb.append("/\n");
                printTree(sb, file, prefix + (isLast ? "    " : "│   "));
            } else {
                sb.append("\n");
            }
        }
    }
}
