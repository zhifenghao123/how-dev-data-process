package com.howdev.flinkdev.biz.logstat.myoperator.processWindow;


import com.howdev.flinkdev.biz.logstat.dto.AggregateDimension;
import com.howdev.flinkdev.biz.logstat.dto.AggregateMetric;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResult;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResultMiddle;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Map;

@Slf4j
public class LogProcessWindowFunction extends ProcessWindowFunction<AggregateResultMiddle<AggregateDimension, AggregateMetric>,
        AggregateResult<AggregateDimension, AggregateMetric>, Tuple3<Long, String, String>, TimeWindow> {

    private final OutputTag<AggregateResult<AggregateDimension, AggregateMetric>> sideOutputTag;

    public LogProcessWindowFunction(OutputTag<AggregateResult<AggregateDimension, AggregateMetric>> sideOutputTag) {
        this.sideOutputTag = sideOutputTag;
    }

    @Override
    public void process(Tuple3<Long, String, String> key, ProcessWindowFunction<AggregateResultMiddle<AggregateDimension, AggregateMetric>, AggregateResult<AggregateDimension, AggregateMetric>, Tuple3<Long, String, String>, TimeWindow>.Context context, Iterable<AggregateResultMiddle<AggregateDimension, AggregateMetric>> elements, Collector<AggregateResult<AggregateDimension, AggregateMetric>> out) throws Exception {
        long requestTimeStamp = key.f0;
        String service = key.f1;
        String serviceIp = key.f2;

        AggregateDimension dimension = new AggregateDimension();
        dimension.setService(service);
        dimension.setServiceIp(serviceIp);


        AggregateResultMiddle<AggregateDimension, AggregateMetric> aggregateResultMiddle = elements.iterator().next();

        AggregateResult<AggregateDimension, AggregateMetric> detectResult = new AggregateResult<>();
        detectResult.setRequestTimeStamp(requestTimeStamp);
        detectResult.setDimension(dimension);
        detectResult.setMetric(aggregateResultMiddle.getMetric());
        out.collect(detectResult);


        Map<AggregateDimension, AggregateMetric> drillDownAggregateResultMap = aggregateResultMiddle.getDrillDownAggregateResultMap();
        for (Map.Entry<AggregateDimension, AggregateMetric> entry : drillDownAggregateResultMap.entrySet()) {
            AggregateResult<AggregateDimension, AggregateMetric> drillDownResult = new AggregateResult<>();
            drillDownResult.setRequestTimeStamp(requestTimeStamp);
            drillDownResult.setDimension(entry.getKey());
            drillDownResult.setMetric(entry.getValue());
            // 按千分之一概率输出日志
            if (Math.random() < 1) {
                log.info("抽样输出 - drillDownResult: {}", drillDownResult);
            }
            context.output(sideOutputTag, drillDownResult);
        }
    }
}