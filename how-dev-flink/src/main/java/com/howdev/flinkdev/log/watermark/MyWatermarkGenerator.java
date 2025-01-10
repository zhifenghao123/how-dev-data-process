package com.howdev.flinkdev.log.watermark;

import com.howdev.flinkdev.log.biz.domain.LogRecord;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;

public class MyWatermarkGenerator implements WatermarkGenerator<LogRecord> {
    // 最大乱序时间
    private static final long maxOutOfOrderness = 10000L;

    private long currentMaxTimestamp;


    @Override
    public void onEvent(LogRecord timeable, long eventTimestamp, WatermarkOutput watermarkOutput) {
        currentMaxTimestamp = Math.max(currentMaxTimestamp, timeable.getEventTimeStamp());
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
        // 允许10秒的乱序
        watermarkOutput.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness));
    }
}
