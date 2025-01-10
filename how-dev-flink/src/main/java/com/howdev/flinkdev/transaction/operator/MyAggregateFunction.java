package com.howdev.flinkdev.transaction.operator;

import com.howdev.flinkdev.transaction.biz.domain.Transaction;
import com.howdev.flinkdev.transaction.biz.dto.TransactionAggregateResult;

import org.apache.flink.api.common.functions.AggregateFunction;

import java.math.BigDecimal;
import java.util.Date;


public class MyAggregateFunction  implements AggregateFunction<Transaction, TransactionAggregateResult, TransactionAggregateResult> {

    @Override
    public TransactionAggregateResult createAccumulator() {
        return new TransactionAggregateResult();
    }

    @Override
    public TransactionAggregateResult add(Transaction value, TransactionAggregateResult accumulator) {
        // 第一条数据进来
        String amount = value.getAmount();
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        if (accumulator.getIncomeCount() == null && accumulator.getIncomeAmount() == null
                && accumulator.getExpensesCount() == null && accumulator.getExpensesAmount() == null) {
            // 根据交易金额的正负，分别统计收入和支出
            if (amountBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                accumulator.setIncomeCount(1);

                accumulator.setIncomeAmount(amountBigDecimal);
            } else {
                accumulator.setExpensesCount(1);
                accumulator.setExpensesAmount(amountBigDecimal);
            }
            accumulator.setLatestOccurredTimeStamp(value.getOccurredTimeStamp());
            accumulator.setLatestOccurredTime(value.getOccurredTime());
            accumulator.setBalance(new BigDecimal(value.getBalance()));
        } else {
            // 根据交易金额的正负，分别统计收入和支出
            if (amountBigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                accumulator.setIncomeCount(accumulator.getIncomeCount() == null ? 1 : accumulator.getIncomeCount() + 1);
                BigDecimal incomeAmount = accumulator.getIncomeAmount() == null ? BigDecimal.ZERO : accumulator.getIncomeAmount();
                accumulator.setIncomeAmount(incomeAmount.add(amountBigDecimal));
            } else {
                accumulator.setExpensesCount(accumulator.getExpensesCount() == null ? 1 : accumulator.getExpensesCount() + 1);
                BigDecimal expensesAmount = accumulator.getExpensesAmount() == null ? BigDecimal.ZERO : accumulator.getExpensesAmount();
                accumulator.setExpensesAmount(expensesAmount.add(amountBigDecimal));
            }

            if (value.getOccurredTimeStamp() > accumulator.getLatestOccurredTimeStamp()) {
                accumulator.setLatestOccurredTimeStamp(value.getOccurredTimeStamp());
                accumulator.setLatestOccurredTime(value.getOccurredTime());
                accumulator.setBalance(new BigDecimal(value.getBalance()));
            }
        }

        return accumulator;
    }

    @Override
    public TransactionAggregateResult getResult(TransactionAggregateResult accumulator) {
        Integer incomeCount = accumulator.getIncomeCount() == null ? 0 : accumulator.getIncomeCount();
        Integer expensesCount = accumulator.getExpensesCount() == null ? 0 : accumulator.getExpensesCount();
        accumulator.setTotalCount(incomeCount + expensesCount);
        return accumulator;
    }

    @Override
    public TransactionAggregateResult merge(TransactionAggregateResult a, TransactionAggregateResult b) {

        BigDecimal incomeAmountA = a.getIncomeAmount() != null ? a.getIncomeAmount() : BigDecimal.ZERO;
        Integer incomeCountA = a.getIncomeCount() != null ? a.getIncomeCount() : 0;
        BigDecimal expensesAmountA = a.getExpensesAmount() != null ? a.getExpensesAmount() : BigDecimal.ZERO;
        Integer expensesCountA = a.getExpensesCount() != null ? a.getExpensesCount() : 0;
        Long latestOccurredTimeStampA = a.getLatestOccurredTimeStamp() != null ? a.getLatestOccurredTimeStamp() : 0L;
        Date latestOccurredTimeA = a.getLatestOccurredTime() != null ? a.getLatestOccurredTime() : new Date();
        BigDecimal balanceA = a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO;

        BigDecimal incomeAmountB = b.getIncomeAmount() != null ? b.getIncomeAmount() : BigDecimal.ZERO;
        Integer incomeCountB = b.getIncomeCount() != null ? b.getIncomeCount() : 0;
        BigDecimal expensesAmountB = b.getExpensesAmount() != null ? b.getExpensesAmount() : BigDecimal.ZERO;
        Integer expensesCountB = b.getExpensesCount() != null ? b.getExpensesCount() : 0;
        Long latestOccurredTimeStampB = b.getLatestOccurredTimeStamp() != null ? b.getLatestOccurredTimeStamp() : 0L;
        Date latestOccurredTimeB = b.getLatestOccurredTime() != null ? b.getLatestOccurredTime() : new Date();
        BigDecimal balanceB = b.getBalance() != null ? b.getBalance() : BigDecimal.ZERO;

        TransactionAggregateResult mergeResult = new TransactionAggregateResult();
        mergeResult.setIncomeAmount(incomeAmountA.add(incomeAmountB));
        mergeResult.setIncomeCount(incomeCountA + incomeCountB);
        mergeResult.setExpensesAmount(expensesAmountA.add(expensesAmountB));
        mergeResult.setExpensesCount(expensesCountA + expensesCountB);

        if (latestOccurredTimeStampA > latestOccurredTimeStampB) {
            mergeResult.setLatestOccurredTimeStamp(latestOccurredTimeStampA);
            mergeResult.setLatestOccurredTime(latestOccurredTimeA);
            mergeResult.setBalance(balanceA);
        } else {
            mergeResult.setLatestOccurredTimeStamp(latestOccurredTimeStampB);
            mergeResult.setLatestOccurredTime(latestOccurredTimeB);
            mergeResult.setBalance(balanceB);
        }
        return mergeResult;
    }
}
