package com.howdev.flinkdev.log.watermark;

import com.howdev.mock.dto.LogRecord;
import org.apache.flink.api.common.eventtime.*;

public class MyWatermarkStrategy implements WatermarkStrategy<LogRecord> {
    @Override
    public WatermarkGenerator<LogRecord> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new MyWatermarkGenerator();
    }

    @Override
    public TimestampAssigner<LogRecord> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return new MyTimestampAssigner();
    }
}
