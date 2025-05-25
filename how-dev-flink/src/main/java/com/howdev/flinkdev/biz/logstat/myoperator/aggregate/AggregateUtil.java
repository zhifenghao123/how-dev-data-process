package com.howdev.flinkdev.biz.logstat.myoperator.aggregate;


import com.howdev.flinkdev.biz.logstat.dto.AggregateDimension;
import com.howdev.flinkdev.biz.logstat.dto.AggregateMetric;
import com.howdev.flinkdev.biz.logstat.dto.AggregateResultMiddle;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AggregateUtil {


    public static AggregateMetric initCalculateAggregateMetric(AggregateMetric valueMetric){
        AggregateMetric metric = new AggregateMetric();
        // (1) 总请求数
        metric.setTotalCount(valueMetric.getTotalCount());
        // （2）不同秒的qps
        metric.setSecondToCountMap(valueMetric.getSecondToCountMap());

        return metric;
    }

    public static AggregateMetric mergeCalculateAggregateMetric(AggregateMetric accumulatorMetric, AggregateMetric valueMetric){
        AggregateMetric mergedMetric = new AggregateMetric();

        // (1) 总请求数
        mergedMetric.setTotalCount(accumulatorMetric.getTotalCount() + valueMetric.getTotalCount());

        // （2）不同秒的qps
        Map<Long, Long> accumulatorSecondToCountMap = accumulatorMetric.getSecondToCountMap();
        Map<Long, Long> valueSecondToCountMap = valueMetric.getSecondToCountMap();

        // 合并不同秒时间戳的个数
        Map<Long, Long> mergedSecondToCountMap = Stream.concat(
                        accumulatorSecondToCountMap.entrySet().stream(),
                        valueSecondToCountMap.entrySet().stream()
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Optional.ofNullable(entry.getValue()).orElse(0L),
                        Long::sum
                ));
        mergedMetric.setSecondToCountMap(mergedSecondToCountMap);

        return mergedMetric;
    }

    public static Map<AggregateDimension, AggregateMetric> initAndPutDrillDownAggregateResultMap(AggregateDimension valueDimension, AggregateMetric valueMetric) {
        Map<AggregateDimension, AggregateMetric> drillDownAggregateResultMap = new HashMap<>();
        drillDownAggregateResultMap.put(valueDimension, valueMetric);
        return drillDownAggregateResultMap;
    }

    public static Map<AggregateDimension, AggregateMetric> putAndMergeDrillDownAggregateResultMap(AggregateDimension valueDimension, AggregateMetric valueMetric, Map<AggregateDimension, AggregateMetric> drillDownAggregateResultMap) {
        if (drillDownAggregateResultMap == null) {
            // 正常能进入putAndMergeDrillDownAggregateResultMap方法的，一定是add方法中，accumulator.getDrillDownAggregateResultMap()不为空，
            log.error("drillDownAggregateResultMap is null");
            return initAndPutDrillDownAggregateResultMap(valueDimension, valueMetric);
        }

        AggregateMetric accumulatorMetric = drillDownAggregateResultMap.get(valueDimension);
        if (accumulatorMetric == null) {
            drillDownAggregateResultMap.put(valueDimension, valueMetric);
        } else {
            AggregateMetric mergedMetric = mergeCalculateAggregateMetric(accumulatorMetric, valueMetric);
            drillDownAggregateResultMap.put(valueDimension, mergedMetric);
        }
        return drillDownAggregateResultMap;
    }

    public static Tuple2<Long, Long> getMaxOfValuePerSecond(Map<Long, Long> secondToValueMap) {
        long maxVps = 0;
        long maxVpsSecond = 0;
        for (Map.Entry<Long, Long> entry : secondToValueMap.entrySet()) {
            if (entry.getValue() > maxVps) {
                maxVpsSecond = entry.getKey();
                maxVps = entry.getValue();
            }
        }
        return new Tuple2<>(maxVpsSecond, maxVps);
    }

    public static void calculateDrillDownMaxVps(AggregateResultMiddle<AggregateDimension, AggregateMetric> accumulator, Long maxQpsTimeStamp)   {
        Map<AggregateDimension, AggregateMetric> drillDownAggregateResultMap = accumulator.getDrillDownAggregateResultMap();
        for (Map.Entry<AggregateDimension, AggregateMetric> entry : drillDownAggregateResultMap.entrySet()) {
            AggregateMetric metric = entry.getValue();
            Long maxQps = getDrillDownMaxVps(metric.getSecondToCountMap(), maxQpsTimeStamp);
            metric.setMaxQps(maxQps);
        }
    }

    public static Long getDrillDownMaxVps(Map<Long, Long> secondToValueMap, Long maxVpsTimeStamp) {
        if (secondToValueMap == null || secondToValueMap.isEmpty()) {
            return 0L;
        }
        return secondToValueMap.get(maxVpsTimeStamp);
    }
}