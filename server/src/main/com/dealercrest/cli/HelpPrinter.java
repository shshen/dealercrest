package com.dealercrest.cli;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

public class HelpPrinter {

    public void print(Command cmd, String path) {
        if (cmd != null) {
            System.out.println("\nUsage: " + path + " [options]");
        }

        System.out.println("\nOptions:");
        OptionParser optionResolver = new OptionParser();
        List<Option> options = optionResolver.parse(cmd);
        printOptions(options);
    }

    public void print(Class<?> cls, String path) {

        Command cmd = cls.getAnnotation(Command.class);

        // -------- Default execute() options --------
        Method executeMethod = findExecuteMethod(cls);
        if (executeMethod != null) {
            if (executeMethod.getParameterCount() > 0) {
                System.out.println("Usage:  " + path + " [options] <command>");
                System.out.println("\nOptions:");
                OptionParser optionResolver = new OptionParser();
                List<Option> options = optionResolver.parse(cmd);
                printOptions(options);
            }
        } else {
            System.out.println("Usage:  " + path + " <command>");
        }

        System.out.println();
        Map<String, List<Command>> commandMap = new LinkedHashMap<>();

        // -------- Method subcommands --------
        for (Method m : cls.getDeclaredMethods()) {
            Command sub = m.getAnnotation(Command.class);
            if (sub != null ) {
                add(commandMap, sub);
            }
        }

        // -------- Children class subcommands --------
        Command classCmd = cls.getAnnotation(Command.class);
        if (classCmd != null) {
            Include nestedCmd = cls.getAnnotation(Include.class);
            if ( nestedCmd != null) {
                for (Class<?> child : nestedCmd.value()) {
                    Command childCmd = child.getAnnotation(Command.class);
                    if (childCmd != null) {
                        add(commandMap, childCmd);
                    } else {
                        for (Method m : child.getDeclaredMethods()) {
                            Command sub = m.getAnnotation(Command.class);
                            if (sub != null) {
                                add(commandMap, sub);
                            }
                        }
                    }
                }
            }

        }
        printSubCommands(classCmd, commandMap);
    }

    private void add(Map<String, List<Command>> map, Command cmd) {
        if (cmd.hidden()) {
            return;
        }
        String group = cmd.group();
        List<Command> actions = map.get(group);
        if (actions == null) {
            actions = new ArrayList<>();
            map.put(group, actions);
        }
        actions.add(cmd);
    }

    private void printSubCommands(Command parentCmd, Map<String, List<Command>> map) {
        if (map.size() == 1 && map.containsKey("General")) {
            List<Command> actions = map.get("General");
            System.out.println(parentCmd.desc());
            printSubCommands(actions);
            return;
        }

        Set<Entry<String,List<Command>>> dddd = map.entrySet();
        for(Entry<String, List<Command>> d: dddd) {
            String group = d.getKey();
            System.out.println(group + " commands");
            List<Command> actions = d.getValue();
            printSubCommands(actions);
        }
    }

    private void printSubCommands(List<Command> actions) {
        for (Command action : actions) {
            String subName = action.name();
            String desc = action.desc();
    
            if (subName.length() > 18) {
                System.out.println("  [" + subName+"]");
                if (desc != null && !desc.isEmpty()) {
                    System.out.printf("%21s%s%n", "", desc);
                }
            } else {
                System.out.printf("  %-19s %s%n",
                        subName,
                        desc != null ? desc : "");
            }
        }
    }

    private Method findExecuteMethod(Class<?> cls) {
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals("execute")) {
                return m;
            }
        }
        return null;
    }

    private void printOptions(Collection<Option> optList) {
        for (Option o : optList) {
            String required_desc = "  " + o.desc();
            if (o.required()) {
                required_desc = "* " + o.desc();
            }
            String[] allowed = o.allowedValues();
            if (allowed.length > 0) {
                String result = "[" + String.join(",", allowed) + "]";
                required_desc = required_desc + result;
            }
            if (o.defaultValue() != null && !o.defaultValue().isEmpty()) {
                required_desc = required_desc + " (default:" + o.defaultValue() + ")";
            }
            String longOptValue = o.longName();

            if (o.type() != boolean.class) {
                longOptValue = longOptValue + "=";
            }

            String sss = "     --" + longOptValue;
            if (!o.shortName().isEmpty()) {
                sss = "  -" + o.shortName() + "," + "--" + longOptValue;
            }

            if (sss.length() > 18) {
                sss = sss + "\n";
                sss = sss + String.format("%20s", "") + required_desc;
            } else {
                int spaces = 20 - sss.length();
                sss = sss + String.format("%" + spaces + "s", "") + required_desc;
            }
            System.out.println(sss);
        }
    }

}
