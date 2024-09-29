package com.howdev.flinkdev.transaction.watermark;

import com.howdev.flinkdev.transaction.biz.domain.Transaction;
import org.apache.flink.api.common.eventtime.TimestampAssigner;

public class MyTimestampAssigner implements TimestampAssigner<Transaction> {
    @Override
    public long extractTimestamp(Transaction transaction, long l) {
        return transaction.getOccurredTimeStamp();
    }
}
