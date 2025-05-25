package com.howdev.flinkdev.flink.watermark;

import com.howdev.flinkdev.flink.domain.MyAbstractEvent;
import org.apache.flink.api.common.eventtime.*;

public class MyWatermarkStrategy<T extends MyAbstractEvent> implements WatermarkStrategy<T> {

    // 最大乱序时间
    private final long maxOutOfOrderNess;

    public MyWatermarkStrategy() {
        // 没有显示指定最大乱序时间，则默认为0L，即不允许乱序，必须严格有序。
        this.maxOutOfOrderNess = 0L;
    }

    public MyWatermarkStrategy(long maxOutOfOrderNess) {
        if (maxOutOfOrderNess < 0) {
            throw new IllegalArgumentException("maxOutOfOrderNess must be non-negative");
        }
        this.maxOutOfOrderNess = maxOutOfOrderNess;
    }

    @Override
    public WatermarkGenerator<T> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new MyWatermarkGenerator<>(maxOutOfOrderNess);
    }

    @Override
    public TimestampAssigner<T> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return new MyTimestampAssigner<>();
    }
}
