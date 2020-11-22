package com.ELang.ELang;

import java.util.Arrays;

public class Interpreter {
    CodeFeature[] codeFeatures;
    Interpreter interpreterMain;
    String name;
    String args;
    boolean debug;

    Interpreter(String text, boolean debug, String name, String args) {
        this.name = name;
        this.args = args;
        Interpret(splitNewlines(toLF(text)), debug);
    }

    Interpreter(String text, boolean debug, String name, Interpreter interpreterMain, CodeFeature[] startWith) {
        this.interpreterMain = interpreterMain;
        this.name = name;
        InterpretFunction(splitNewlines(toLF(text), startWith), debug);
    }

    String toLF(String str) {
        str = str.replace("\r\n", "\n");
        str = str.replace("\r", "\n");
        return str;
    }

    String toCRLF(String str) {
        str = str.replace("\n", "\r\n");
        str = str.replace("\r", "\r\n");
        return str;
    }

    String toCR(String str) {
        str = str.replace("\n", "\r");
        str = str.replace("\r\n", "\r");
        return str;
    }

    CodeFeature[] splitNewlines(String text) {
        return splitNewlines(text, null);
    }

    CodeFeature[] splitNewlines(String text, CodeFeature[] startWith) {
        CodeFeature[] toReturn = startWith != null ? startWith : new CodeFeature[] {};
        int line = 1;
        char[] charArray = text.toCharArray();
        boolean inBlock = false;
        boolean inString = false;
        StringBuilder curLine = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            if (inBlock) {
                if (charArray[i] == '}') {
                    inBlock = false;
                    toReturn[toReturn.length - 1].infoMap.put("code", curLine.toString());
                    curLine = new StringBuilder();
                } else {
                    curLine.append(charArray[i]);
                }
            } else if (charArray[i] == '{') {
                inBlock = true;
                toReturn = Arrays.copyOf(toReturn, toReturn.length + 1);
                toReturn[toReturn.length - 1] = new CodeFeature(curLine.toString(), line);
                curLine = new StringBuilder();
            } else if (charArray[i] == '\n') {
                if (curLine.toString().matches(".+\\(.*\\)")) {
                    toReturn = Arrays.copyOf(toReturn, toReturn.length + 1);
                    toReturn[toReturn.length - 1] = new CodeFeature(CodeFeatureType.FUNCTIONCALL, line, curLine.toString());
                } else if (curLine.toString().matches(".+\\s*=\\s*.+")) {
                    for (CodeFeature codeFeature : toReturn) {
                        if (codeFeature.type == CodeFeatureType.VARIABLE) {
                            if (codeFeature.infoMap.get("name").equals(curLine.toString().replaceAll("(\\s|\\t)*(?=.+\\s*=\\s*.+)", "").replaceAll("\\s*=\\s*.+", ""))) {
                                codeFeature.infoMap.put("value", curLine.toString().replaceAll(".+\\s*=\\s*", "").replaceAll("(?<!\\\\)\"", "").replaceAll("(?<!\\\\)'", "").replaceAll("\\\\\"", "\"").replaceAll("\\\\'", "'"));
                            } else {
                                toReturn = Arrays.copyOf(toReturn, toReturn.length + 1);
                                toReturn[toReturn.length - 1] = new CodeFeature(CodeFeatureType.VARIABLE, line, curLine.toString());
                            }
                        }
                    }
                }
                line++;
                curLine = new StringBuilder();
            } else {
                curLine.append(charArray[i]);
            }
            CodeFeature.localCodeFeatures = toReturn;
        }
        return toReturn;
    }

    void InterpretFunction(CodeFeature[] codeFeatures, boolean debug) {
        this.codeFeatures = codeFeatures;
        this.debug = debug;
        for (int i = 0; i < codeFeatures.length; i++) {
            if (codeFeatures[i].type == CodeFeatureType.FUNCTIONCALL) {
                if (((String) codeFeatures[i].infoMap.get("name")).matches(".+\\..+")) {
                    if (GetGlobalNamespace(((String) codeFeatures[i].infoMap.get("name")).replaceAll("\\..+", ""))) {
                        if (GetGlobalNamespaceFunction(((String) codeFeatures[i].infoMap.get("name")).replaceAll("\\..+", ""), ((String) codeFeatures[i].infoMap.get("name")).replaceAll(".+\\.", ""))) {
                            RunGlobalNamespaceFunction(((String) codeFeatures[i].infoMap.get("name")).replaceAll("\\..+", ""), ((String) codeFeatures[i].infoMap.get("name")).replaceAll(".+\\.", ""), (String[]) codeFeatures[i].infoMap.get("params"));
                        } else {
                            System.err.println("No such function '".concat(((String) codeFeatures[i].infoMap.get("name")).replaceAll(".+(?=\\.)", "")).concat("' in namespace '").concat(((String) codeFeatures[i].infoMap.get("name")).replaceAll("\\..+", "")).concat("'\n\tin function '").concat(name).concat("()'\n\tin file '".concat(interpreterMain.name).concat("'\n\ton line ").concat(Integer.toString(codeFeatures[i].line + 1))));
                            System.exit(-1);
                        }
                    } else {
                        System.err.println("No such namespace '".concat(((String) codeFeatures[i].infoMap.get("name")).replaceAll("\\..+", "")).concat("'\n\tin function '").concat(name).concat("()'\n\tin file '".concat(interpreterMain.name).concat("'\n\ton line ").concat(Integer.toString(codeFeatures[i].line + 1))));
                        System.exit(-1);
                    }
                } else {
                    if (interpreterMain.CheckFunction((String) codeFeatures[i].infoMap.get("name"))) {
                        interpreterMain.RunFunction((String) codeFeatures[i].infoMap.get("name"), (String[]) codeFeatures[i].infoMap.get("params"));
                    } else {
                        System.err.println("No such function '".concat((String) codeFeatures[i].infoMap.get("name")).concat("'\n\tin function '").concat(name).concat("()'\n\tin file '".concat(interpreterMain.name).concat("'\n\ton line ").concat(Integer.toString(codeFeatures[i].line + 1))));
                        System.exit(-1);
                    }
                }
            }
        }
    }

    void Interpret(CodeFeature[] codeFeatures, boolean debug) {
        CodeFeature.globalCodeFeatures = codeFeatures;
        this.codeFeatures = codeFeatures;
        this.debug = debug;
        if (!CheckFunction("Main")) {
            System.err.println("No 'Main()' function");
            System.exit(-1);
        } else {
            RunFunction("Main", new String[] {args});
        }
    }

    boolean CheckFunction(String name) {
        boolean toReturn = false;
        for (int i = 0; i < codeFeatures.length; i++) {
            if (codeFeatures[i].type == CodeFeatureType.FUNCTION) {
                if (codeFeatures[i].infoMap.get("name").equals(name)) {
                    toReturn = true;
                }
            }
        }
        return toReturn;
    }

    void RunFunction(String name, String[] params) {
        for (int i = 0; i < codeFeatures.length; i++) {
            if (codeFeatures[i].type == CodeFeatureType.FUNCTION) {
                if (codeFeatures[i].infoMap.get("name").equals(name)) {
                    if (debug) {
                        System.out.println("Starting function '".concat(name).concat("()'"));
                    }
                    CodeFeature[] startWith = new CodeFeature[] {};
                    if (params.length != ((String[]) codeFeatures[i].infoMap.get("params")).length) {
                        System.err.println("Function call with incorrect number of arguments");
                        System.exit(-1);
                    }
                    for (int j = 0; j < params.length; j++) {
                        startWith = Arrays.copyOf(startWith, startWith.length + 1);
                        startWith[startWith.length - 1] = new CodeFeature(CodeFeatureType.VARIABLE, -1, "");
                        startWith[startWith.length - 1].infoMap.put("name", ((String[]) codeFeatures[i].infoMap.get("params"))[j]);
                        startWith[startWith.length - 1].infoMap.put("value", params[j]);
                    }
                    Interpreter functionInterpreter = new Interpreter((String) codeFeatures[i].infoMap.get("code"), debug, name, this, startWith);
                }
            }
        }
    }

    boolean GetGlobalNamespace(String name) {
        if (name.equals("Console")) {
            return true;
        }
        return false;
    }

    boolean GetGlobalNamespaceFunction(String namespace, String function) {
        if (namespace.equals("Console")) {
            if (function.equals("writeLine")) {
                return true;
            }
        }
        return false;
    }

    void RunGlobalNamespaceFunction(String namespace, String function, String[] params) {
        if (namespace.equals("Console")) {
            if (function.equals("writeLine")) {
                GlobalFuncs.Console.writeLine(params);
            }
        }
    }
}