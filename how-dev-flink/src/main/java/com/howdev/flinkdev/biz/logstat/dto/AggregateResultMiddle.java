package com.howdev.flinkdev.biz.logstat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregateResultMiddle<D extends AggregateDimension, M extends AggregateMetric> {
    /**
     * 请求时间戳
     */
    long requestTimeStamp;

    D dimension;

    M metric;

    Map<D, M> drillDownAggregateResultMap;

}
