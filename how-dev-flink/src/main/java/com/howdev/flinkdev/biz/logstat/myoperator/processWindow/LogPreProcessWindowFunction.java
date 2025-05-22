package com.howdev.flinkdev.biz.logstat.myoperator.processWindow;


import com.howdev.flinkdev.biz.logstat.dto.AggregateDimension;
import com.howdev.flinkdev.biz.logstat.dto.AggregateMetric;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResult;
import com.howdev.flinkdev.common.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

@Slf4j
public class LogPreProcessWindowFunction extends ProcessWindowFunction<AggregateResult<AggregateDimension, AggregateMetric>,
        AggregateResult<AggregateDimension, AggregateMetric>, Tuple5<String, String, String, String, Integer>, TimeWindow> {

    @Override
    public void process(Tuple5<String, String, String, String, Integer> key, ProcessWindowFunction<AggregateResult<AggregateDimension, AggregateMetric>, AggregateResult<AggregateDimension, AggregateMetric>, Tuple5<String, String, String, String, Integer>, TimeWindow>.Context context, Iterable<AggregateResult<AggregateDimension, AggregateMetric>> elements, Collector<AggregateResult<AggregateDimension, AggregateMetric>> out) throws Exception {
        long windowStartTime = context.window().getStart();
        long windowEndTime = context.window().getEnd();

        String formatWindowStartMinuteTimeText = TimeUtil.formatTimeStamp(windowStartTime, "yyyy-MM-dd HH:mm:00");
        long formatWindowStartMinuteTimeStamp = TimeUtil.getTimeStamp(formatWindowStartMinuteTimeText);

        AggregateResult<AggregateDimension, AggregateMetric> aggregateResult = elements.iterator().next();
        aggregateResult.setRequestTimeStamp(formatWindowStartMinuteTimeStamp);

        String service = key.f0;
        String serviceIp = key.f1;
        String method = key.f2;
        String returnCode = key.f3;
        //Integer randomNum = key.f4;

        AggregateDimension dimension = new AggregateDimension();
        dimension.setService(service);
        dimension.setServiceIp(serviceIp);
        dimension.setMethod(method);
        dimension.setReturnCode(returnCode);

        aggregateResult.setDimension(dimension);

        out.collect(aggregateResult);
    }
}