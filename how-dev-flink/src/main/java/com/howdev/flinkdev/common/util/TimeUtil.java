package com.howdev.flinkdev.common.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class TimeUtil {
    public static String parseEventTime(String rawTimestamp) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = inputFormat.parse(rawTimestamp);
        return outputFormat.format(date);
    }

    public static long getTimeStamp(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static long getTimeStamp(String dateTime, String pattern) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(pattern));
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static String getCurrDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String formatTimeStamp(long timestamp, String pattern) {
        // Convert the Unix timestamp to Date
        Date date = new Date(timestamp);

        // Format the Date to a string
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    public static Date parseDateTime(String dateTimeText, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeText, formatter);
        return Date.from(dateTime.toInstant(ZoneOffset.of("+8")));
    }


    public static void main(String[] args) {
        //long currentTimeMillis = System.currentTimeMillis();
        long currentTimeMillis = 1722935091000l;
        long currentTimeMillis2 = 1722873638266l;


        System.out.println(formatTimeStamp(currentTimeMillis, "yyyy-MM-dd HH:mm:ss"));
        System.out.println(formatTimeStamp(currentTimeMillis, "yyyy-MM-dd HH:mm:00"));

        System.out.println("--------------------");
        long timeStamp = getTimeStamp("2024-08-06 17:04:51", "yyyy-MM-dd HH:mm:00");
        System.out.println(timeStamp);
        String formatWindowStartTime = TimeUtil.formatTimeStamp(timeStamp, "yyyy-MM-dd HH:mm:00");

        System.out.println(formatWindowStartTime);

        // 将formatWindowStartTime转换为Date对象
        Date date = parseDateTime(formatWindowStartTime, "yyyy-MM-dd HH:mm:ss");
        System.out.println(date);
        System.out.println(date.getTime());

        System.out.println("--------------------");
        System.out.println(formatTimeStamp(1722935040000L, "yyyy-MM-dd HH:mm:ss"));
    }

}
