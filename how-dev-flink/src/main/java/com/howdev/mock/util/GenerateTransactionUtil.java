package com.howdev.mock.util;

import com.howdev.mock.dto.Transaction;

import java.text.DateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GenerateTransactionUtil {

    public static long generateLongVal(long[] userIdRange){
        Random random = new Random();
        long userId = random.nextLong() % (userIdRange[1] - userIdRange[0] + 1) + userIdRange[0];
        return userId;
    }

    public static double generateDoubleVal(double[] amountRange){
        Random random = new Random();
        double amount = random.nextDouble() * (amountRange[1] - amountRange[0]) + amountRange[0];
        return amount;
    }

    public static String generateStringValFromEnumVals(String[] typeEnum){
        Random random = new Random();
        int index = random.nextInt(typeEnum.length);
        return typeEnum[index];
    }

    /**
     * 生成一个位于开始时间和结束时间之间的随机时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 生成的随机时间
     */
    public static LocalDateTime generateRandomTime(LocalDateTime startTime, LocalDateTime endTime) {
        // 确保结束时间晚于开始时间
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }

        // 计算时间差（秒）
        long secondsBetween = ChronoUnit.SECONDS.between(startTime, endTime);

        // 检查秒数差是否超出int范围（理论上不应该，但这里作为安全检查）
        if (secondsBetween > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("时间差太大，无法处理");
        }

        // 生成一个0到secondsBetween之间的随机秒数（包括边界）
        Random random = new Random();
        int randomSeconds = random.nextInt((int) (secondsBetween + 1));

        // 使用这个时间差（秒）加上开始时间得到随机时间
        return startTime.plusSeconds(randomSeconds);
    }


    public static Transaction generateTransaction(){
        Transaction transaction = new Transaction();
        transaction.setUserId(generateLongVal(Transaction.USER_ID_RANGE));
        transaction.setAmount(generateDoubleVal(Transaction.AMOUNT_RANGE));
        transaction.setType(generateStringValFromEnumVals(Transaction.TYPE_ENUM));
        transaction.setOccurredLocation(generateStringValFromEnumVals(Transaction.LOCATION_ENUM));

        LocalDateTime localDateTime = generateRandomTime(Transaction.START_TIME, Transaction.END_TIME);

        // 转换为java.util.Date
        // 默认时区，通常是系统时区
        ZoneId defaultZoneId = ZoneId.systemDefault();
        // LocalDateTime转ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(defaultZoneId);
        // ZonedDateTime转Instant
        Date date = Date.from(zonedDateTime.toInstant());

        transaction.setOccurredTime(date);
        return transaction;
    }
}
