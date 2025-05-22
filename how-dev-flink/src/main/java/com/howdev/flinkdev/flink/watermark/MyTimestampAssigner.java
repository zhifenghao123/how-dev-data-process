package com.howdev.flinkdev.flink.watermark;

import com.howdev.flinkdev.flink.domain.MyAbstractEvent;
import org.apache.flink.api.common.eventtime.TimestampAssigner;

public class MyTimestampAssigner<T extends MyAbstractEvent> implements TimestampAssigner<T> {

    @Override
    public long extractTimestamp(T element, long recordTimestamp) {
        return element.getEventTimeStamp();
    }
}
