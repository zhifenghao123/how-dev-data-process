package com.howdev.flinkdev.log.operator;

import com.howdev.flinkdev.log.biz.dto.LogRecordAggregateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.Date;

@Slf4j
public class MyProcessWindowFunction extends ProcessWindowFunction<LogRecordAggregateResult, LogRecordAggregateResult, Tuple3<String, String, Integer>, TimeWindow> {
    @Override
    public void process(Tuple3<String, String, Integer> key, ProcessWindowFunction<LogRecordAggregateResult, LogRecordAggregateResult, Tuple3<String, String, Integer>, TimeWindow>.Context context, Iterable<LogRecordAggregateResult> iterable, Collector<LogRecordAggregateResult> collector) throws Exception {
        log.info("MyProcessWindowFunction->process: key=" + key + ",windowStartTime=" + context.window().getStart() + ",windowEndTime=" + context.window().getEnd());

        long windowStartTime = context.window().getStart();

        String formatWindowStartMinuteTimeText = DateFormatUtils.format(windowStartTime, "yyyy-MM-dd HH:mm:00");

        Date formatWindowStartMinuteTime = DateUtils.parseDate(formatWindowStartMinuteTimeText, "yyyy-MM-dd HH:mm:ss");


        log.info("windowStartTime: {}", formatWindowStartMinuteTimeText);

        LogRecordAggregateResult logRecordAggregateResult = iterable.iterator().next();
        logRecordAggregateResult.setRequestMinuteTime(formatWindowStartMinuteTime);
        logRecordAggregateResult.setRequestMinuteTimeStamp(formatWindowStartMinuteTime.getTime());

        collector.collect(logRecordAggregateResult);
    }
}
