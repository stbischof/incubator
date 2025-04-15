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
package org.eclipse.osgi.technology.incubator.command.help;

import java.io.InputStreamReader;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.osgi.technology.incubator.command.util.GlobFilter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.Justif;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Help {
    private final BundleContext context;

    public Help(BundleContext bc, DTOFormatter formatter) {
        context = bc;
        formatter.build(Scope.class).inspect().field("name").field("description").format("commands", Scope::commands)
                .line().field("name").format("commands", Scope::commands).part().as(Scope::toString);

        formatter.build(Command.class).inspect().as(Command::inspect).line().as(Command::line).part()
                .as(Command::toString);
    }

    @Descriptor("Clear the screen")
    public void cls(CommandSession session) {
        session.getConsole().append("\u001B[2J").flush();
    }

    @Descriptor("Format in box form or turn it off")
    public boolean box(boolean on) {
        return DTOFormatter.boxes = on;
    }

    static public class Scope implements Comparable<Scope> {
        public String name;
        public String description = "";
        public final Map<String, Command> commands = new TreeMap<>();

        Scope(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String commands() {
            var size = commands.size();
            try (var f = new Formatter()) {
                var column = 0;
                for (Command command : commands.values()) {
                    f.format("%-20s ", command.name);
                    if (column++ == 4) {
                        f.format("\n");
                        column = 0;
                    }
                }
                return f.toString();
            }
        }

        @Override
        public int compareTo(Scope o) {
            return name.compareTo(o.name);
        }
    }

    static public class Command {
        String name;
        List<Method> methods = new ArrayList<>();
        Properties properties;

        Command(String name) {
            this.name = name;
        }

        public String inspect() {
            var j = new Justif(100, tabs(8));

            var f = j.formatter();
            f.format("COMMAND\n\t1%s", name);
            var title = getTitle();
            if (title != null) {
                f.format("\t3-- %s\n", fixup(title));
            }
            f.format("\n\u2007\n");
            var topDescription = fixup(getDescription());
            if (topDescription != null) {
                f.format("DESCRIPTION\n");
                f.format("\t1%s\n", topDescription);
            }
            f.format("\u2007\nSYNOPSIS %s\n", name);
            for (Method m : methods) {
                f.format("\t1%s", name);
                var description = Help.getDescription(m);
                if (description != null) {
                    f.format("\t6%s", description);
                }
                f.format("\n");

                var parameters = m.getParameters();
                var mdescription = m.getAnnotation(Descriptor.class);
                for (java.lang.reflect.Parameter p : parameters) {
                    if (p.getType() == CommandSession.class) {
                        continue;
                    }
                    var ann = p.getAnnotation(Parameter.class);
                    var d = p.getAnnotation(Descriptor.class);
                    if (ann != null) {
                        f.format("\t1    [");
                        for (String name : ann.names()) {
                            f.format("%s", name);
                            break;
                        }
                        if (Parameter.UNSPECIFIED.equals(ann.presentValue())) {
                            // not a flag
                            f.format(" %s:%s", p.getName(), type(p.getParameterizedType()));
                        } else {
                            f.format(" %s", p.getName());
                        }
                        f.format("]");
                        f.format("\t4%s", ann.absentValue());
                    } else {
                        if (p.isVarArgs()) {
                            f.format("\t1    %s...:%s", p.getName(), type(p.getType().getComponentType()));
                        } else {
                            f.format("\t1    %s:%s", p.getName(), type(p.getParameterizedType()));
                        }
                    }
                    if (d != null) {
                        f.format("\t6- %s", d.value());
                    }
                    f.format("\n");
                }
                f.format("\n\n");
            }

            var example = getExample();
            if (example != null) {
                f.format("EXAMPLE\n\t1%s\n", example);
            }
            var see = getSee();
            if (see != null) {
                f.format("SEE\n\t1%s\n", see);
            }
            f.format("\n");
            return j.toString();
        }

        private String getExample() {
            return getProperties().getProperty(name + ".example");
        }

        private String getSee() {
            return getProperties().getProperty(name + ".see");
        }

        private String getDescription() {
            return getProperties().getProperty(name + ".description");
        }

        private String getTitle() {
            return getProperties().getProperty(name + ".title", Help.getDescription(methods.get(0)));
        }

        private Properties getProperties() {
            if (properties == null) {
                List<Class<?>> collect = methods.stream().map(Method::getDeclaringClass).filter(Objects::nonNull)
                        .collect(Collectors.toList());
                properties = getResource("help.properties", collect);
            }
            return properties;
        }

        private String type(Type type) {
            if (type instanceof Class) {

                Class<?> clazz = (Class<?>) type;

                if (clazz.isPrimitive()) {
                    return clazz.getName();
                }

                if (clazz.isArray()) {
                    return type(clazz.getComponentType()) + "[]";
                }

                return clazz.getSimpleName().toLowerCase();
            } else if (type instanceof ParameterizedType) {

            }
            if (type instanceof ParameterizedType ptype) {
                var raw = type(ptype.getRawType());
                var sb = new StringBuilder();
                sb.append(raw).append("<");
                for (Type t : ptype.getActualTypeArguments()) {
                    sb.append(type(t));
                }
                sb.append(">");
                return sb.toString();
            }
            return type.toString().toLowerCase();
        }

        public String line() {
            return inspect();
        }
    }

    static public class Argument {
        public String name;
        public String description;
        public Type type;
    }

    @Descriptor("Displays help for each available scopes")
    public Collection<Scope> help() throws Exception {
        return getCommands().values();
    }

    @Descriptor("Displays help for a command")
    public List<Command> help(
    // @formatter:off
		@Parameter(names= {"-s","--scope"}, absentValue="*")
		@Descriptor("Limit to matching scopes")
		GlobFilter scope,
		@Descriptor("Glob for the command name")
		GlobFilter command
	// @formatter:on
    ) throws Exception {
        return getCommands().entrySet().stream().filter(e -> scope.matches(e.getKey()))
                .flatMap(e -> e.getValue().commands.entrySet().stream()).filter(e -> command.matches(e.getKey()))
                .map(Entry::getValue).collect(Collectors.toList());
    }

    private Map<String, Scope> getCommands() throws Exception {
        Map<String, Scope> scopes = new TreeMap<>();

        ServiceReference<?>[] refs = null;
        try {
            refs = context.getAllServiceReferences(null, "(osgi.command.scope=*)");
        } catch (InvalidSyntaxException ex) {
            throw new RuntimeException(ex);
        }

        for (ServiceReference<?> ref : refs) {
            Object svc = context.getService(ref);
            if (svc != null) {
                try {
                    var description = getDescription(svc.getClass());
                    var name = (String) ref.getProperty("osgi.command.scope");
                    var scope = scopes.computeIfAbsent(name, Scope::new);
                    scope.description = concat(scope.description, description);

                    var ofunc = ref.getProperty("osgi.command.function");
                    String[] funcs = null;
                    if (ofunc instanceof String s) {
                        funcs = new String[] { s };
                    } else if (ofunc instanceof String[] sarr) {
                        funcs = sarr;
                    } else if (ofunc instanceof Collection<?> c) {
                        funcs = c.toArray(new String[0]);
                    }

                    for (String func : funcs) {
                        scope.commands.computeIfAbsent(func, Command::new);
                    }

                    var methods = svc.getClass().getMethods();
                    for (Method method : methods) {
                        for (String func : funcs) {
                            if (matches(func, method)) {
                                var command = scope.commands.get(func);
                                command.methods.add(method);
                            }
                        }
                    }

                } finally {
                    context.ungetService(ref);
                }
            }
        }
        return scopes;
    }

    private boolean matches(String func, Method method) {
        func = func.toLowerCase();
        var name = method.getName().toLowerCase();
        if (func.equals(name)) {
            return true;
        }
        if (name.startsWith("_")) {
            name = name.substring(1);
        }

        if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }

        return func.equals(name);
    }

    private String concat(String description, String description2) {
        if (description == null) {
            return description2;
        }
        if (description2 == null) {
            return description;
        }

        return description.concat("\n").concat(description2);
    }

    private static String getDescription(AnnotatedElement class1) {
        var descriptor = class1.getAnnotation(Descriptor.class);
        if (descriptor == null) {
            return null;
        }

        return descriptor.value();
    }

    private static int[] tabs(int width) {
        var tabs = new int[100];
        for (var i = 0; i < 100; i++) {
            tabs[i] = i * width;
        }
        return tabs;
    }

    private static Properties getResource(String path, Class<?> clazz) {
        try (var in = clazz.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }

            try (var rd = new InputStreamReader(in)) {
                var p = new Properties();
                p.load(rd);
                return p;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static Properties getResource(String name, Collection<Class<?>> clazz) {
        return clazz.stream().map(c -> getResource(name, c)).filter(Objects::nonNull).findFirst()
                .orElse(new Properties());
    }

    private static String fixup(String s) {
        if (s == null) {
            return null;
        }
        // the boxing causes empty lines to be stripped
        return s.replace("\n\n", "\n\u2007\n").replace('\n', '\f');
    }
}
