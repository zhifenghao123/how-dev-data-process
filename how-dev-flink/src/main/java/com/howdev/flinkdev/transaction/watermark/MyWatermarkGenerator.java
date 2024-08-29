package com.howdev.flinkdev.transaction.watermark;

import com.howdev.mock.dto.Transaction;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;

public class MyWatermarkGenerator implements WatermarkGenerator<Transaction> {
    // 最大乱序时间
    private static final long maxOutOfOrderness = 10000L;

    private long currentMaxTimestamp;


    @Override
    public void onEvent(Transaction transaction, long eventTimestamp, WatermarkOutput watermarkOutput) {
        currentMaxTimestamp = Math.max(currentMaxTimestamp, transaction.getOccurredTimeStamp());
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
        // 允许10秒的乱序
        watermarkOutput.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness));
    }
}
