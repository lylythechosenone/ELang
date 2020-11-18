package com.ELang.ELang;

import java.util.Arrays;

public class Interpreter {
    CodeFeature[] codeFeatures;
    Interpreter interpreterMain;
    String name;
    boolean debug;

    Interpreter(String text, boolean debug, String name) {
        this.name = name;
        Interpret(splitNewlines(toLF(text)), debug);
    }

    Interpreter(String text, boolean debug, String name, Interpreter interpreterMain) {
        this.interpreterMain = interpreterMain;
        this.name = name;
        InterpretFunction(splitNewlines(toLF(text)), debug);
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
        CodeFeature[] toReturn = {};
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
                    toReturn = Arrays.copyOf(toReturn, toReturn.length + 1);
                    toReturn[toReturn.length - 1] = new CodeFeature(CodeFeatureType.VARIABLE, line, curLine.toString());
                }
                line++;
                curLine = new StringBuilder();
            } else {
                curLine.append(charArray[i]);
            }
        }
        return toReturn;
    }

    void InterpretFunction(CodeFeature[] codeFeatures, boolean debug) {
        CodeFeature.localCodeFeatures = codeFeatures;
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
            RunFunction("Main", new String[] {""});
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
                    Interpreter functionInterpreter = new Interpreter((String) codeFeatures[i].infoMap.get("code"), debug, name, this);
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
                for (String param : params) {
                    System.out.println(param);
                }
            }
        }
    }
}