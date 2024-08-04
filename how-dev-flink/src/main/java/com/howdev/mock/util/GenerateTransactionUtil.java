package com.howdev.mock.util;

import com.howdev.mock.dto.Transaction;

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


    public static Transaction generateTransaction(){
        Transaction transaction = new Transaction();
        transaction.setUserId(generateLongVal(Transaction.USER_ID_RANGE));
        transaction.setAmount(generateDoubleVal(Transaction.AMOUNT_RANGE));
        transaction.setType(generateStringValFromEnumVals(Transaction.TYPE_ENUM));
        transaction.setOccurredLocation(generateStringValFromEnumVals(Transaction.LOCATION_ENUM));
        return transaction;
    }
}
