package com.howdev.flinkdev.transaction.watermark;

import com.howdev.mock.dto.Transaction;
import org.apache.flink.api.common.eventtime.*;

public class MyWatermarkStrategy implements WatermarkStrategy<Transaction> {
    @Override
    public WatermarkGenerator<Transaction> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
        return new MyWatermarkGenerator();
    }

    @Override
    public TimestampAssigner<Transaction> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
        return new MyTimestampAssigner();
    }
}
