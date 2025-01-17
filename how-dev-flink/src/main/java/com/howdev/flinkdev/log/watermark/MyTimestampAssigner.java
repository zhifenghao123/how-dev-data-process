package com.howdev.flinkdev.log.watermark;

import com.howdev.flinkdev.log.biz.domain.LogRecord;
import org.apache.flink.api.common.eventtime.TimestampAssigner;

public class MyTimestampAssigner implements TimestampAssigner<LogRecord> {
    @Override
    public long extractTimestamp(LogRecord timeable, long l) {
        return timeable.getEventTimeStamp();
    }
}
