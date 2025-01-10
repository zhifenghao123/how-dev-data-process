package com.howdev.flinkdev.log.operator;

import com.howdev.flinkdev.log.biz.domain.LogRecord;
import com.howdev.flinkdev.log.biz.dto.LogRecordAggregateResult;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.AggregateFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MyAggregateFunction implements AggregateFunction<LogRecord, LogRecordAggregateResult, LogRecordAggregateResult> {

    @Override
    public LogRecordAggregateResult createAccumulator() {
        return new LogRecordAggregateResult();
    }

    @Override
    public LogRecordAggregateResult add(LogRecord value, LogRecordAggregateResult accumulator) {
        log.info("MyAggregateFunction->add");
        if (accumulator.getCount() == null) {
           accumulator.setService(value.getService());
            accumulator.setMethod(value.getMethod());

            accumulator.setCount(1);
            accumulator.setTotalTime(value.getCost());

            Map<Integer, Integer> codeToCntMap = new HashMap<>();
            codeToCntMap.put(Integer.valueOf(value.getReturnCode()), 1);
            accumulator.setCodeToCountMap(codeToCntMap);
        } else {
            accumulator.setCount(accumulator.getCount() + 1);
            accumulator.setTotalTime(accumulator.getTotalTime() + value.getCost());

            Map<Integer, Integer> codeToCntMap = accumulator.getCodeToCountMap();
            Integer code = Integer.valueOf(value.getReturnCode());
            if (codeToCntMap.containsKey(code)) {
                codeToCntMap.put(code, codeToCntMap.get(code) + 1);
            } else {
                codeToCntMap.put(code, 1);
            }
            accumulator.setCodeToCountMap(codeToCntMap);
        }
        return accumulator;

    }

    @Override
    public LogRecordAggregateResult getResult(LogRecordAggregateResult accumulator) {
        log.info("MyAggregateFunction->getResult");
        // 计算平均耗时
        double avgTimeCost = 0.0;
        try {
            avgTimeCost = (double) accumulator.getTotalTime() / accumulator.getCount();
        } catch (Exception e) {
            log.error("avgTimeCost error, totalTimeCost:{}, cnt: {}", accumulator.getTotalTime(), accumulator.getCount());
        }

        accumulator.setAvgTime(avgTimeCost);
        return accumulator;
    }

    @Override
    public LogRecordAggregateResult merge(LogRecordAggregateResult a, LogRecordAggregateResult b) {
        log.info("MyAggregateFunction->merge");
        LogRecordAggregateResult mergeResult = new LogRecordAggregateResult();
        mergeResult.setService(a.getService());
        mergeResult.setMethod(a.getMethod());

        mergeResult.setCount(a.getCount() + b.getCount());
        mergeResult.setTotalTime(a.getTotalTime() + b.getTotalTime());

        Map<Integer, Integer> codeExtra1 = a.getCodeToCountMap();
        Map<Integer, Integer> codeExtra2 = b.getCodeToCountMap();
        // 合并不同码值的个数
        Map<Integer, Integer> mergedMap = Stream.concat(
                        codeExtra1.entrySet().stream(),
                        codeExtra2.entrySet().stream()
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Optional.ofNullable(entry.getValue()).orElse(0), // 将null视为0
                        Integer::sum
                ));

        mergeResult.setCodeToCountMap(mergedMap);

        return mergeResult;
    }
}
