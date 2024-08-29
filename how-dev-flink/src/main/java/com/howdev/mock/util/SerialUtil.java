package com.howdev.mock.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class SerialUtil {
    public static Map<String, String> parseJson(String jsonString) throws Exception {
        Map<String, String> jsonMap = new HashMap<>();
        jsonString = jsonString.trim();
        if (!jsonString.startsWith("{") || !jsonString.endsWith("}")) {
            throw new Exception("invalid json string");
        }
        jsonString = jsonString.substring(1, jsonString.length() - 1);
        String[] keyValuePairs = jsonString.split(",");
        for (String pair : keyValuePairs) {
            pair = pair.trim();
            int colonIndex = pair.indexOf(':');
            if (colonIndex <= 0) {
                throw new Exception("invalid json string");
            }
            //String key = pair.substring(0, colonIndex).trim();
            //String value = pair.substring(colonIndex + 1).trim();
//            if (!value.startsWith("\"") || !value.endsWith("\"")) {
//                throw new Exception("invalid json string");
//            }
            String key = pair.substring(0, colonIndex).trim().replace("\"", "");
            String value = pair.substring(colonIndex + 1).trim().replace("\"", "");

            jsonMap.put(key, value);
        }
        return jsonMap;
    }

    public static void main(String[] args) throws Exception {
        String jsonString = "{\"id\":\"0\",\"userId\":\"7422\",\"amount\":\"18317.15\",\"type\":\"工资\",\"balance\":\"118317.15\",\"occurredLocation\":\"江西省南昌市\",\"occurredTime\":\"Thu Aug 29 13:27:08 CST 2024\",\"occurredTimeStamp\":\"1724909228902\"}\n";
        Map<String, String> jsonMap = parseJson(jsonString);
        System.out.println(jsonMap);
    }
}
