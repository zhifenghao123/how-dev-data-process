package com.howdev.flinkdev.biz.logstat.myoperator.aggregate;


import com.howdev.flinkdev.biz.logstat.domain.LogRecord;
import com.howdev.flinkdev.biz.logstat.dto.AggregateDimension;
import com.howdev.flinkdev.biz.logstat.dto.AggregateMetric;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LogPreAggregateFunction implements AggregateFunction<LogRecord, AggregateResult<AggregateDimension, AggregateMetric>, AggregateResult<AggregateDimension, AggregateMetric>> {
    @Override
    public AggregateResult<AggregateDimension, AggregateMetric> createAccumulator() {
        return new AggregateResult<>();
    }

    @Override
    public AggregateResult<AggregateDimension, AggregateMetric> add(LogRecord value, AggregateResult<AggregateDimension, AggregateMetric> accumulator) {
        // 将13位的毫秒级时间戳requestTimeStamp转为10位的秒级时间戳
        long secondTimeStamp = value.getRequestTimeStamp() / 1000;

        if (accumulator.getMetric() == null) {
            // 初始化metric
            // (1) 总请求数
            AggregateMetric metric = new AggregateMetric();
            metric.setTotalCount(1L);

            // （2）不同秒的qps
            Map<Long, Long> secondToCountMap = new HashMap<>();
            secondToCountMap.put(secondTimeStamp, 1L);
            metric.setSecondToCountMap(secondToCountMap);

            accumulator.setMetric(metric);
        } else {
            AggregateMetric metric = accumulator.getMetric();

            // (1) 总请求数
            metric.setTotalCount(metric.getTotalCount() + 1);

            // （2）不同秒的qps
            Map<Long, Long> secondToCountMap = metric.getSecondToCountMap();
            long currentSecondCountExistCount = secondToCountMap.get(secondTimeStamp) == null ? 0L : secondToCountMap.get(secondTimeStamp);
            secondToCountMap.put(secondTimeStamp, currentSecondCountExistCount + 1);
            metric.setSecondToCountMap(secondToCountMap);
            accumulator.setMetric(metric);
        }
        return accumulator;
    }

    @Override
    public AggregateResult<AggregateDimension, AggregateMetric> getResult(AggregateResult<AggregateDimension, AggregateMetric> accumulator) {
        return accumulator;
    }

    @Override
    public AggregateResult<AggregateDimension, AggregateMetric> merge(AggregateResult<AggregateDimension, AggregateMetric> a, AggregateResult<AggregateDimension, AggregateMetric> b) {
        return null;
    }
}
