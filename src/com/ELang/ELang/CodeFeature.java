package com.ELang.ELang;

import java.util.HashMap;
import java.util.Map;

public class CodeFeature {
    CodeFeatureType type;
    String info;
    Map<String, Object> infoMap;
    int line;
    static int stringCodes = 0;
    static CodeFeature[] globalCodeFeatures;
    static CodeFeature[] localCodeFeatures;

    CodeFeature(CodeFeatureType type, int line, String info) {
        this.type = type;
        this.info = info;
        this.line = line;
        this.infoMap = InfoMapFromInfo(this);
    }

    CodeFeature(String info, int line) {
        this.type = GetTypeFromInfo(info);
        this.info = info;
        this.line = line;
        this.infoMap = InfoMapFromInfo(this);
    }

    static CodeFeatureType GetTypeFromInfo(String info) {
        if (info.matches(".+\\(.*\\)\\s")) {
            return CodeFeatureType.FUNCTION;
        }
        return null;
    }

    static Map<String, Object> InfoMapFromInfo(CodeFeature feature) {
        Map<String, Object> toReturn = new HashMap<>();
        if (feature.type == CodeFeatureType.FUNCTION) {
            toReturn.put("name", feature.info.replaceAll("\\s*\\(.*\\)\\s*", ""));
            toReturn.put("params", feature.info.replaceAll(".+\\(", "").replaceAll("\\)\\s", "").split(",\\s*"));
        } else if (feature.type == CodeFeatureType.FUNCTIONCALL) {
            toReturn.put("name", feature.info.replaceAll("(\\t|\\s)*(?=[^\\s\\t]+\\s*\\(.*\\))", "").replaceAll("\\s*\\(.*\\)", ""));
            String[] params = feature.info.replaceAll(".+\\(", "").replaceAll("\\)", "").split(",\\s*(?=(?:[^\"']*(\"|')[^\"']*(\"|'))*[^\"']*[^']*$)");
            for (int i = 0; i < params.length; i++) {
                if (params[i].matches("(?<=([^\\\\]|^))\".+[^\\\\]\"") || params[i].matches("(?<=([^\\\\]|^))'.+[^\\\\]'")) {
                    params[i] = params[i].replaceAll("(?<!\\\\)\"", "").replaceAll("(?<!\\\\)'", "").replaceAll("\\\\\"", "\"").replaceAll("\\\\'", "'");
                } else if (params[i].matches(".+\\[[0-9]]")) {
                    for (CodeFeature codeFeature : globalCodeFeatures) {
                        if (codeFeature.type == CodeFeatureType.VARIABLE) {
                            if (codeFeature.infoMap.get("name").equals(params[i])) {
                                params[i] = ((String) codeFeature.infoMap.get("value")).split(",\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")[Integer.parseInt(params[i].replaceAll(".+\\[", "").replaceAll("]", ""))].replaceAll("(?<!\\\\)\"", "").replaceAll("(?<!\\\\)'", "");
                            }
                        }
                    }
                    if (localCodeFeatures != null) {
                        for (CodeFeature codeFeature : localCodeFeatures) {
                            if (codeFeature.type == CodeFeatureType.VARIABLE) {
                                if (codeFeature.infoMap.get("name").equals(params[i].replaceAll("\\[[0-9]]", ""))) {
                                    params[i] = ((String) codeFeature.infoMap.get("value")).split(",\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")[Integer.parseInt(params[i].replaceAll(".+\\[", "").replaceAll("]", ""))].replaceAll("(?<!\\\\)\"", "").replaceAll("(?<!\\\\)'", "");
                                }
                            }
                        }
                    }
                } else if (!params[i].matches("[0-9]")) {
                    for (CodeFeature codeFeature : globalCodeFeatures) {
                        if (codeFeature.type == CodeFeatureType.VARIABLE) {
                            if (codeFeature.infoMap.get("name").equals(params[i])) {
                                params[i] = (String) codeFeature.infoMap.get("value");
                            }
                        }
                    }
                    if (localCodeFeatures != null) {
                        for (CodeFeature codeFeature : localCodeFeatures) {
                            if (codeFeature.type == CodeFeatureType.VARIABLE) {
                                if (codeFeature.infoMap.get("name").equals(params[i])) {
                                    params[i] = (String) codeFeature.infoMap.get("value");
                                }
                            }
                        }
                    }
                }
            }
            toReturn.put("params", params);
        } else if (feature.type == CodeFeatureType.VARIABLE) {
            toReturn.put("name", feature.info.replaceAll("(\\s|\\t)*(?=.+\\s*=\\s*.+)", "").replaceAll("\\s*=\\s*.+", ""));
            toReturn.put("value", feature.info.replaceAll(".+\\s*=\\s*", "").replaceAll("(?<!\\\\)\"", "").replaceAll("(?<!\\\\)'", "").replaceAll("\\\\\"", "\"").replaceAll("\\\\'", "'"));
        }
        return toReturn;
    }
}