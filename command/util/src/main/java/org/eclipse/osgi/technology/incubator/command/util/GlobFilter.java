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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobFilter {

    enum State {
        SIMPLE, CURLIES, BRACKETS, QUOTED
    }

    public static final GlobFilter ALL = new GlobFilter("*");

    private final Pattern pattern;

    public GlobFilter(String globString) {
        this(globString, 0);
    }

    public GlobFilter(String globString, int flags) {
        this(globString, toPattern(globString, flags));
    }

    protected GlobFilter(String globString, Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern pattern() {
        return pattern;
    }

    public static Pattern toPattern(String line) {
        return toPattern(line, 0);
    }

    public static Pattern toPattern(String line, int flags) {
        line = line.trim();
        var strLen = line.length();
        var sb = new StringBuilder(strLen << 2);
        var curlyLevel = 0;
        var state = State.SIMPLE;

        char previousChar = 0;
        for (var i = 0; i < strLen; i++) {
            var currentChar = line.charAt(i);
            switch (currentChar) {
            case '*':
                if ((state == State.SIMPLE || state == State.CURLIES) && !isEnd(previousChar)) {
                    sb.append('.');
                }
                sb.append(currentChar);
                break;
            case '?':
                if ((state == State.SIMPLE || state == State.CURLIES) && !isStart(previousChar)
                        && !isEnd(previousChar)) {
                    sb.append('.');
                } else {
                    sb.append(currentChar);
                }
                break;
            case '+':
                if ((state == State.SIMPLE || state == State.CURLIES) && !isEnd(previousChar)) {
                    sb.append('\\');
                }
                sb.append(currentChar);
                break;
            case '\\':
                sb.append(currentChar);
                if (i + 1 < strLen) {
                    var nextChar = line.charAt(++i);
                    if (state == State.SIMPLE && nextChar == 'Q') {
                        state = State.QUOTED;
                    } else if (state == State.QUOTED && nextChar == 'E') {
                        state = State.SIMPLE;
                    }
                    sb.append(nextChar);
                }
                break;
            case '[':
                if (state == State.SIMPLE) {
                    state = State.BRACKETS;
                }
                sb.append(currentChar);
                break;
            case ']':
                if (state == State.BRACKETS) {
                    state = State.SIMPLE;
                }
                sb.append(currentChar);
                break;

            case '{':
                if ((state == State.SIMPLE || state == State.CURLIES) && !isEnd(previousChar)) {
                    state = State.CURLIES;
                    sb.append("(?:");
                    curlyLevel++;
                } else {
                    sb.append(currentChar);
                }
                break;
            case '}':
                if (state == State.CURLIES && curlyLevel > 0) {
                    sb.append(')');
                    currentChar = ')';
                    curlyLevel--;
                    if (curlyLevel == 0) {
                        state = State.SIMPLE;
                    }
                } else {
                    sb.append(currentChar);
                }
                break;
            case ',':
                if (state == State.CURLIES) {
                    sb.append('|');
                } else {
                    sb.append(currentChar);
                }
                break;
            case '^':
            case '.':
            case '$':
            case '@':
            case '%':
                if (state == State.SIMPLE || state == State.CURLIES) {
                    sb.append('\\');
                }

                // FALL THROUGH
            default:
                sb.append(currentChar);
                break;
            }
            previousChar = currentChar;
        }
        return Pattern.compile(sb.toString(), flags);
    }

    private static boolean isStart(char c) {
        return c == '(';
    }

    private static boolean isEnd(char c) {
        return c == ')' || c == ']';
    }

    public Matcher createMatcher(CharSequence input) {
        return pattern.matcher(input);
    }

    public boolean matches(String s) {
        return matches((CharSequence) s);
    }

    public boolean matches(CharSequence s) {
        return createMatcher(s).matches();
    }
}