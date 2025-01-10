package com.howdev.common.util;

import com.howdev.common.example.domain.LogRecordExample;
import junit.framework.TestCase;

public class JacksonUtilTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
    }

    public void testToJson() {
        String jsonString = "{\n" +
                "    \"requestId\": \"20240919152353522580_495\",\n" +
                "    \"requestTimeStamp\": 1726730633,\n" +
                "    \"service\": \"service5\",\n" +
                "    \"method\": \"method1\",\n" +
                "    \"returnCode\": 0,\n" +
                "    \"cost\": 25\n" +
                "}";
        LogRecordExample logRecord;
        try {
            logRecord = JacksonUtil.fromJson(jsonString, LogRecordExample.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("-------JacksonUtil.toJson-----------");
        String json = JacksonUtil.toJson(logRecord);
        System.out.println(json);
    }

    public void testFromJson() {
        String jsonString = "{\n" +
                "    \"requestId\": \"20240919152353522580_495\",\n" +
                "    \"requestTimeStamp\": 1726730633,\n" +
                "    \"service\": \"service5\",\n" +
                "    \"method\": \"method1\",\n" +
                "    \"returnCode\": 0,\n" +
                "    \"cost\": 25\n" +
                "}";
        try {
            LogRecordExample logRecord = JacksonUtil.fromJson(jsonString, LogRecordExample.class);
            System.out.println(logRecord.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
