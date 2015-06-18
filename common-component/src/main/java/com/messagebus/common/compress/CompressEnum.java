package com.messagebus.common.compress;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanghua on 5/23/15.
 */
public enum CompressEnum {

    SNAPPY,
    LZF,
    LZ4;

    private static Map<String, CompressEnum> lookupMaps = new HashMap<String, CompressEnum>(3);

    static {
        lookupMaps.put("snappy", SNAPPY);
        lookupMaps.put("lzf", LZF);
        lookupMaps.put("lz4", LZ4);
    }

    private CompressEnum() {
    }

    public static CompressEnum lookup(String strVal) {
        if (strVal == null || strVal.isEmpty()) {
            throw new RuntimeException("param : strVal can not be null or empty");
        }

        return lookupMaps.get(strVal.toLowerCase());
    }

    public static String stringVal(CompressEnum compressEnum) {
        if (compressEnum == null) {
            throw new NullPointerException("param : compressEnum can not be null");
        }

        for (Map.Entry<String, CompressEnum> entry : lookupMaps.entrySet()) {
            if (compressEnum.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return "";
    }
}
