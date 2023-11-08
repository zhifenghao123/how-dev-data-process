package com.howdev.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * DateTimeUtil class
 *
 * @author haozhifeng
 * @date 2023/11/07
 */
public class DateTimeUtil {
    /** yyyyMM */
    public static final String yyyyMM_1_FORMAT = "yyyyMM";
    /** yyyy-MM */
    public static final String yyyyMM_2_FORMAT = "yyyy-MM";
    /** yyyy年MM月 */
    public static final String yyyyMM_3_FORMAT = "yyyy年MM月";

    /** yyyyMMdd */
    public static final String yyyyMMdd_1_FORMAT = "yyyyMMdd";
    /** yyyy-MM-dd */
    public static final String yyyyMMdd_2_FORMAT = "yyyy-MM-dd";
    /** yyyy年MM月dd日 */
    public static final String yyyyMMdd_3_FORMAT = "yyyy年MM月dd日";

    /** HHmmss */
    public static final String HHmmss_1_FORMAT = "HHmmss";
    /** HH:mm:ss */
    public static final String HHmmss_2_FORMAT = "HH:mm:ss";

    /** yyyyMMddHHmmss */
    public static final String yyyyMMddHHmmss_1_FORMAT = "yyyyMMddHHmmss";

    /** yyyy-MM-dd HH:mm:ss */
    public static final String yyyyMMddHHmmss_2_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** yyyyMMddHHmmssSSS */
    public static final String yyyyMMddHHmmssSSS_1_FORMAT = "yyyyMMddHHmmssSSS";
    /** yyyy-MM-dd HH:mm:ss:SSS */
    public static final String yyyyMMddHHmmssSSS_2_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";

    /**
     * 默认系统格式
     */
    public static final String DEFAULT_SYSTEM_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

    private static List<String> formatPatterns =
            Arrays.asList(yyyyMM_1_FORMAT, yyyyMM_2_FORMAT, yyyyMM_3_FORMAT,
                    yyyyMMdd_1_FORMAT, yyyyMMdd_2_FORMAT, yyyyMMdd_3_FORMAT,
                    HHmmss_1_FORMAT, HHmmss_2_FORMAT,
                    yyyyMMddHHmmss_1_FORMAT, yyyyMMddHHmmss_2_FORMAT,
                    yyyyMMddHHmmssSSS_1_FORMAT, yyyyMMddHHmmssSSS_2_FORMAT,
                    DEFAULT_SYSTEM_FORMAT);

    /**
     * transferLocalDateTimeToDate
     *
     * @param localDateTime localDateTime
     * @return:
     * @author: haozhifeng
     */
    private static Date transferLocalDateTimeToDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            throw new IllegalArgumentException("无效的时间: null");
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        Instant instant = zonedDateTime.toInstant();
        Date date = Date.from(instant);
        return date;
    }

    /**
     * transferDateToLocalDateTime
     *
     * @param date date
     * @return:
     * @author: haozhifeng
     */
    private static LocalDateTime transferDateToLocalDateTime(Date date) {
        if (null == date) {
            throw new IllegalArgumentException("无效的时间: null");
        }
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }

    public static String formatNow(String formatString) {
        // 创建一个 LocalDateTime 对象
        LocalDateTime nowLocalDateTime = LocalDateTime.now();
        String formattedDateTime = format(nowLocalDateTime, formatString);
        return formattedDateTime;
    }

    public static String format(Date date, String formatString) {
        LocalDateTime localDateTime = transferDateToLocalDateTime(date);
        String formattedDateTime = format(localDateTime, formatString);
        return formattedDateTime;
    }

    public static String format(LocalDateTime localDateTime, String formatString) {
        if (null == localDateTime) {
            throw new IllegalArgumentException("无效的时间: null");
        }
        if (!formatPatterns.contains(formatString)) {
            throw new IllegalArgumentException("无效的格式:" + formatString);
        }
        // 创建一个 DateTimeFormatter 对象，用于格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
        // 使用 DateTimeFormatter 将 LocalDateTime 格式化为字符串
        String formattedDateTime = localDateTime.format(formatter);
        return formattedDateTime;
    }

    public static LocalDateTime parseToLocalDateTime(String dateTimeString, String formatString) {
        if (null == dateTimeString || dateTimeString.isEmpty()) {
            throw new IllegalArgumentException("无效的时间字符串: " + dateTimeString);
        }
        if (!formatPatterns.contains(formatString)) {
            throw new IllegalArgumentException("无效的格式:" + formatString);
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatString);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter);
        return localDateTime;
    }

    public static Date parseToDate(String dateTimeString, String formatString) {
        LocalDateTime localDateTime = parseToLocalDateTime(dateTimeString, formatString);
        Date date = transferLocalDateTimeToDate(localDateTime);
        return date;
    }

}
