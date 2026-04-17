package com.dealercrest.cli;

import java.lang.reflect.*;
import java.util.*;


public class ShellExecutor {

    private final HelpPrinter help = new HelpPrinter();
    private final TokenParser tokenParser = new TokenParser();
    private final OptionParser optionParser = new OptionParser();

    public void run(Object rootCommand, String[] args) throws Exception {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        Object current = rootCommand;
        PathChain pathChain = new PathChain().add("#");

        // Step 1: Walk down the command tree
        while (!argList.isEmpty()) {
            String nextToken = argList.get(0);

            Class<?> cls = current.getClass();
            // 1️, Check current method subcommands
            Method mmm = findMethod(current.getClass(), nextToken);
            if (mmm != null) {
                argList.remove(0);
                pathChain.add(nextToken);
                Command methodCmd = mmm.getAnnotation(Command.class);
                invokeMethod(pathChain, current, mmm, methodCmd, argList);
                return;
            }

            // 2️, Check include children classes
            boolean matched = false;
            // Command classCmd = cls.getAnnotation(Command.class);
            Include nestedCmd = cls.getAnnotation(Include.class);
            if (nestedCmd != null) {
                for (Class<?> child : nestedCmd.value()) {
                    Command childCmd = child.getAnnotation(Command.class);
                    if (childCmd != null) {
                        if (childCmd.name().equals(nextToken)) {
                            argList.remove(0);
                            pathChain.add(childCmd.name());
                            current = child.getDeclaredConstructor().newInstance();
                            matched = true;
                            break;
                        }
                    } else {
                        for (Method m : child.getDeclaredMethods()) {
                            Command sub = m.getAnnotation(Command.class);
                            if (sub != null && sub.name().equals(nextToken)) {
                                current = child.getDeclaredConstructor().newInstance();
                                argList.remove(0);
                                pathChain.add(sub.name());
                                invokeMethod(pathChain, current, m, sub, argList);
                                return;
                            }
                        }
                    }
                }
            }
            if (!matched) {
                break; // no deeper command, stop walking
            }
        }

        // Step 2: Execute default action
        Class<?> cls = current.getClass();
        try {
            Method execute = cls.getMethod("execute", RunContext.class);
            Command clsCmd = cls.getAnnotation(Command.class);
            invokeMethod(pathChain, current, execute, clsCmd, argList);
        } catch (NoSuchMethodException e) {
            System.err.println("Error: No execute() method found for " + cls.getSimpleName());
            help.print(cls, pathChain.join());
        }
    }

    private Method findMethod(Class<?> cls, String name) {
        for (Method m : cls.getDeclaredMethods()) {
            Command cmd = m.getAnnotation(Command.class);
            if (cmd != null && cmd.name().equals(name)) {
                return m;
            }
        }
        return null;
    }

    private void invokeMethod(PathChain pathName, Object target, Method method, Command cmd, List<String> argList)
            throws IllegalAccessException, InvocationTargetException {
        Map<String, String> pppp = tokenParser.parse(argList);
        List<Option> options = optionParser.parse(cmd);
        RunContext runContext = new RunContext(pathName.join(), options, pppp);

        Collection<Option> values = runContext.getOptions();
        for (Option o : values) {
            String longName = o.longName();
            String value = runContext.get(longName);
            if (o.required() && value.isEmpty()) {
                System.out.println("Error: " + longName + " is required");
                String methodName = method.getName();
                if ("execute".equals(methodName)) {
                    help.print(target.getClass(), pathName.join());
                } else {
                    help.print(cmd, pathName.join());
                }
                return;
            }
            String[] allowedValues = o.allowedValues();
            if (allowedValues.length > 0) {
                boolean allowed = false;
                for (String v: allowedValues) {
                    if ( v.equals(value)) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    System.out.println("Error: invalid value");
                    help.print(cmd, pathName.join());
                    return;
                }
            }
        }

        if ( runContext.getBool("help")) {
            if ("execute".equals(method.getName())) {
                help.print(target.getClass(), pathName.join());
            } else {
                help.print(cmd, pathName.join());
            }
            return;
        }

        try {
            method.invoke(target, runContext);
        } catch (Exception e) {
            Throwable p = getParentIllegalException(e);
            if ( p!=null ) {
                String msg = p.getMessage();
                System.out.println("Error: " + msg);
                help.print(cmd, pathName.join());
            } else {
                e.printStackTrace();
            }
        }
    }

    private Throwable getParentIllegalException(Throwable cause) {
        int max_depth = 5;
        Throwable result = cause;
        while (result != null && max_depth>0) {
            if (result instanceof IllegalArgumentException) {
                return result;
            }
            max_depth = max_depth - 1;
            result = result.getCause();
        }
        return null;
    }

}
