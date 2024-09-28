package com.howdev.mock.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtil {
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
}
