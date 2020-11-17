package com.ELang.ELang;

import java.util.Arrays;

public class Interpreter {
    Interpreter(String text, boolean debug) {
        Interpret(splitNewlines(toLF(text)), debug);
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
        char[] charArray = text.toCharArray();
        boolean inBlock = false;
        StringBuilder curLine = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            if (inBlock) {
                if (charArray[i] == '}') {
                    inBlock = false;
                    toReturn[toReturn.length - 1].infoMap.put("code", curLine.toString());
                } else {
                    curLine.append(charArray[i]);
                }
            } else if (charArray[i] == '{') {
                inBlock = true;
                toReturn = Arrays.copyOf(toReturn, toReturn.length + 1);
                toReturn[toReturn.length - 1] = new CodeFeature(curLine.toString());
                curLine = new StringBuilder();
            } else if (curLine.toString().matches(".+\\(.*\\)\n")) {
                toReturn = Arrays.copyOf(toReturn, toReturn.length + 1);
                toReturn[toReturn.length - 1] = new CodeFeature(CodeFeatureType.FUNCTIONCALL, curLine.toString());
            } else if (charArray[i] == '\n') {
                curLine = new StringBuilder();
            } else {
                curLine.append(charArray[i]);
            }
        }
        return toReturn;
    }
    void Interpret(CodeFeature[] codeFeatures, boolean debug) {
        for (int i = 0; i < codeFeatures.length; i++) {
            System.out.println(i);
            System.out.println(codeFeatures[i].type + ": " + codeFeatures[i].infoMap.get("name"));
            if (codeFeatures[i].type == CodeFeatureType.FUNCTION) {
                System.out.println("{" + codeFeatures[i].infoMap.get("code") + "}");
            }
        }
    }
}