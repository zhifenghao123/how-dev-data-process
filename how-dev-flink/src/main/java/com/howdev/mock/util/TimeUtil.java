package com.howdev.mock.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public static String formatTimeStamp(long timestamp, String pattern) {
        // Convert the Unix timestamp to Date
        Date date = new Date(timestamp);

        // Format the Date to a string
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }
}
