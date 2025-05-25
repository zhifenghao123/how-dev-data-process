package com.howdev.flinkdev.flink.watermark;

import com.howdev.flinkdev.flink.domain.MyAbstractEvent;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;

public class MyWatermarkGenerator<T extends MyAbstractEvent> implements WatermarkGenerator<T> {

    // 默认最大乱序时间，2000毫秒。
    private static final long DEFAULT_MAX_OUT_OF_ORDER_NESS = 2000L;

    // 最大乱序时间
    private final long maxOutOfOrderNess;

    private long currentMaxTimestamp;

    public MyWatermarkGenerator() {
        // 默认最大乱序时间为2000毫秒。
        maxOutOfOrderNess = DEFAULT_MAX_OUT_OF_ORDER_NESS;
    }

    public MyWatermarkGenerator(long maxOutOfOrderNess) {
        this.maxOutOfOrderNess = maxOutOfOrderNess;
    }

    @Override
    public void onEvent(T event, long eventTimestamp, WatermarkOutput watermarkOutput) {
        currentMaxTimestamp = Math.max(currentMaxTimestamp, event.getEventTimeStamp());
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
        // 允许10秒的乱序
        watermarkOutput.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderNess));
    }
}
