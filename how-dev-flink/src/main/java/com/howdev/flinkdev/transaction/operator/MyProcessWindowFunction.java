package com.howdev.flinkdev.transaction.operator;

import com.howdev.flinkdev.transaction.biz.dto.TransactionAggregateResult;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class MyProcessWindowFunction extends ProcessWindowFunction<TransactionAggregateResult, TransactionAggregateResult, Tuple2<Long, String>, TimeWindow> {
    @Override
    public void process(Tuple2<Long, String> keyTuple, ProcessWindowFunction<TransactionAggregateResult, TransactionAggregateResult, Tuple2<Long, String>, TimeWindow>.Context context, Iterable<TransactionAggregateResult> iterable, Collector<TransactionAggregateResult> collector) throws Exception {
        long windowStartTime = context.window().getStart();
        long windowEndTime = context.window().getEnd();

        String formatWindowStartMinuteTimeText = DateFormatUtils.format(windowStartTime, "yyyy-MM-dd HH:mm:00");

        Long userId = keyTuple.f0;
        String occurredLocation = keyTuple.f1;

        TransactionAggregateResult transactionAggregateResult = iterable.iterator().next();
        transactionAggregateResult.setOccurredMinuteTimeStamp(windowEndTime);
        transactionAggregateResult.setOccurredMinuteTimeText(formatWindowStartMinuteTimeText);
        transactionAggregateResult.setUserId(userId);
        transactionAggregateResult.setOccurredLocation(occurredLocation);

        collector.collect(transactionAggregateResult);
    }
}
