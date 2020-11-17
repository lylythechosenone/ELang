package com.ELang.ELang;

import java.util.HashMap;
import java.util.Map;

public class CodeFeature {
    CodeFeatureType type;
    String info;
    Map<String, Object> infoMap;

    CodeFeature(CodeFeatureType type, String info) {
        this.type = type;
        this.info = info;
        this.infoMap = InfoMapFromInfo(this);
    }

    CodeFeature(String info) {
        this.type = getTypeFromInfo(info);
        this.info = info;
        this.infoMap = InfoMapFromInfo(this);
    }

    static CodeFeatureType getTypeFromInfo(String info) {
        if (info.matches(".+\\(.+\\)\\s")) {
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
            toReturn.put("name", feature.info.replaceAll("\\s*\\(.*\\)", ""));
            toReturn.put("params", feature.info.replaceAll(".+\\(", "").replaceAll("\\)", "").split(",\\s*"));
        }
        return toReturn;
    }
}