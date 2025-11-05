package com.pixelcrater.Diaro.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class KeyValuePair {

    public final String key;
    public final String value;

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValue(ArrayList<KeyValuePair> arrayList, String key) {
        for (KeyValuePair keyValuePair : arrayList) {
            if (StringUtils.equalsIgnoreCase(keyValuePair.key, key)) {
                return keyValuePair.value;
            }
        }
        return null;
    }

}
