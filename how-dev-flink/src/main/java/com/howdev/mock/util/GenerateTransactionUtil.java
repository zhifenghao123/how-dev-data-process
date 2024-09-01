package com.howdev.mock.util;

import com.howdev.mock.dto.Transaction;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GenerateTransactionUtil {
    private static Map<Long, BigDecimal> userIdToBalanceMap = new HashMap<>();
    static {
        for (long userId = Transaction.USER_ID_RANGE[0]; userId <= Transaction.USER_ID_RANGE[1]; userId++) {
            userIdToBalanceMap.put(userId, new BigDecimal(Transaction.INIT_BALANCE));
        }
    }

    public static long generateLongVal(long[] userIdRange){
        Random random = new Random();
        long userId = (random.nextLong() & Long.MAX_VALUE) % (userIdRange[1] - userIdRange[0] + 1) + userIdRange[0];
        return userId;
    }

    public static BigDecimal generateBigDecimalVal(BigDecimal[] amountRange){
        Random random = new Random();
        BigDecimal range = amountRange[1].subtract(amountRange[0]);
        BigDecimal amount = amountRange[0].add(range.multiply(BigDecimal.valueOf(random.nextDouble())));
        return amount;
    }

    public static String generateStringValFromEnumVals(String[] enumVals){
        Random random = new Random();
        int index = random.nextInt(enumVals.length);
        return enumVals[index];
    }

    public static Transaction generateTransaction(){

        long userId = generateLongVal(Transaction.USER_ID_RANGE);
        userIdToBalanceMap.putIfAbsent(userId, new BigDecimal(Transaction.INIT_BALANCE));
        BigDecimal balance = userIdToBalanceMap.get(userId);

        BigDecimal amount = BigDecimal.ZERO;
        String type = "";

        // 随机生成支出类型和支出金额
        String expensesType = generateStringValFromEnumVals(Transaction.EXPENSES_TYPE_ENUM);

        String expensesAmountRange = Transaction.EXPENSES_TYPE_TO_AMOUNT_RANGE.get(expensesType);

        String[] expensesAmountRangeArr = expensesAmountRange.split("-");
        BigDecimal[] parseExpensesAmountRange = new BigDecimal[2];
        parseExpensesAmountRange[0] = new BigDecimal(expensesAmountRangeArr[0]);
        parseExpensesAmountRange[1] = new BigDecimal(expensesAmountRangeArr[1]);

        BigDecimal expensesAmount = generateBigDecimalVal(parseExpensesAmountRange);

        // 随机生成收入类型 和 收入金额
        String incomeType = generateStringValFromEnumVals(Transaction.INCOME_TYPE_ENUM);

        String incomeAmountRange = Transaction.INCOME_TYPE_TO_AMOUNT_RANGE.get(incomeType);

        String[] incomeAmountRangeArr = incomeAmountRange.split("-");
        BigDecimal[] parseIncomeAmountRange = new BigDecimal[2];
        parseIncomeAmountRange[0] = new BigDecimal(incomeAmountRangeArr[0]);
        parseIncomeAmountRange[1] = new BigDecimal(incomeAmountRangeArr[1]);

        BigDecimal incomeAmount = generateBigDecimalVal(parseIncomeAmountRange);

        // 按照概率生成支出或收入
        boolean isExpenses = new Random().nextBoolean();
        if (isExpenses) {

            // 如果余额充足
            if (balance.compareTo(expensesAmount) > 0) {
                amount = expensesAmount.negate();
                balance = balance.subtract(expensesAmount);
                type = expensesType;
            } else {
                // 余额不足，就生成收入交易
                balance = balance.add(incomeAmount);
                amount = incomeAmount;
                type = incomeType;
            }
        } else {
            balance = balance.add(incomeAmount);
            amount = incomeAmount;
            type = incomeType;
        }

        userIdToBalanceMap.put(userId, balance);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
        transaction.setType(type);
        transaction.setOccurredLocation(generateStringValFromEnumVals(Transaction.LOCATION_ENUM));

        // Date date = new Date();
        // transaction.setOccurredTime(date);
        transaction.setBalance(balance.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
        return transaction;
    }

    public static void main(String[] args) throws Exception {
        Transaction transaction = generateTransaction();
        Date occurredTime = new Date();
        transaction.setOccurredTime(occurredTime);
        transaction.setOccurredTimeStamp(occurredTime.getTime());
        String jsonString = transaction.toJsonString();
        System.out.println(jsonString);

        Transaction transaction2 = new Transaction(jsonString);
        System.out.println(transaction2.toJsonString());
    }
}
