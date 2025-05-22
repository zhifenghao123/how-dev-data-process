package com.howdev.flinkdev.biz.logstat.myoperator.aggregate;


import com.howdev.flinkdev.biz.logstat.dto.AggregateDimension;
import com.howdev.flinkdev.biz.logstat.dto.AggregateMetric;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResult;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResultMiddle;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.Map;

public class LogAggregateFunction implements AggregateFunction<AggregateResult<AggregateDimension, AggregateMetric>, AggregateResultMiddle<AggregateDimension, AggregateMetric>, AggregateResultMiddle<AggregateDimension, AggregateMetric>> {
    @Override
    public AggregateResultMiddle<AggregateDimension, AggregateMetric> createAccumulator() {
        return new AggregateResultMiddle<>();
    }

    @Override
    public AggregateResultMiddle<AggregateDimension, AggregateMetric> add(AggregateResult<AggregateDimension, AggregateMetric> value, AggregateResultMiddle<AggregateDimension, AggregateMetric> accumulator) {
        AggregateMetric valueMetric = value.getMetric();
        AggregateDimension valueDimension = value.getDimension();

        if (accumulator.getMetric() == null) {
            AggregateMetric metric = AggregateUtil.initCalculateAggregateMetric(valueMetric);
            Map<AggregateDimension, AggregateMetric> drillDownAggregateResultMap = AggregateUtil.initAndPutDrillDownAggregateResultMap(valueDimension, metric);

            accumulator.setMetric(metric);
            accumulator.setDrillDownAggregateResultMap(drillDownAggregateResultMap);
        } else {
            AggregateMetric accumulatorMetric = accumulator.getMetric();

            AggregateMetric mergedMetric = AggregateUtil.mergeCalculateAggregateMetric(accumulatorMetric, valueMetric);

            Map<AggregateDimension, AggregateMetric> drillDownAggregateResultMap = AggregateUtil.putAndMergeDrillDownAggregateResultMap(valueDimension, valueMetric, accumulator.getDrillDownAggregateResultMap());

            accumulator.setMetric(mergedMetric);
            accumulator.setDrillDownAggregateResultMap(drillDownAggregateResultMap);
        }

        return accumulator;
    }

    @Override
    public AggregateResultMiddle<AggregateDimension, AggregateMetric> getResult(AggregateResultMiddle<AggregateDimension, AggregateMetric> accumulator) {
        AggregateMetric metric = accumulator.getMetric();

        // 计算本分钟内最大QPS
        Map<Long, Long> secondToCountMap = metric.getSecondToCountMap();
        Tuple2<Long, Long> maxQps = AggregateUtil.getMaxOfValuePerSecond(secondToCountMap);

        metric.setMaxQps(maxQps.f1);


        AggregateUtil.calculateDrillDownMaxVps(accumulator, maxQps.f0);

        return accumulator;
    }

    @Override
    public AggregateResultMiddle<AggregateDimension, AggregateMetric> merge(AggregateResultMiddle<AggregateDimension, AggregateMetric> a, AggregateResultMiddle<AggregateDimension, AggregateMetric> b) {
        return null;
    }
}
