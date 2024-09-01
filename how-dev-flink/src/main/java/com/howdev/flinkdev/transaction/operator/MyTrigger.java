package com.howdev.flinkdev.transaction.operator;

import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * 自定义触发器，解决最后一个窗口没有数据来，无法关闭的问题
 */
public class MyTrigger extends Trigger<Object, TimeWindow> {
    @Override
    public TriggerResult onElement(Object element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {
        if (window.maxTimestamp() <= ctx.getCurrentWatermark()) {
            // 迟到数据
            return TriggerResult.FIRE;
        } else {
            ctx.registerEventTimeTimer(window.maxTimestamp());
            ctx.registerProcessingTimeTimer(window.maxTimestamp() + 10000L);
            return TriggerResult.CONTINUE;
        }
    }

    @Override
    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
        if (time == window.maxTimestamp()) {
            ctx.deleteProcessingTimeTimer(window.maxTimestamp() + 10000L);
            return TriggerResult.FIRE;
        } else {
            return TriggerResult.CONTINUE;
        }
    }

    @Override
    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
        ctx.deleteEventTimeTimer(window.maxTimestamp());
        return TriggerResult.FIRE;
    }

    @Override
    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
        ctx.deleteEventTimeTimer(window.maxTimestamp());
        ctx.deleteProcessingTimeTimer(window.maxTimestamp() + 10000L);
    }
}
