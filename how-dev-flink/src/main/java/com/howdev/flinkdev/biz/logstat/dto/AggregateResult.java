package com.howdev.flinkdev.biz.logstat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregateResult<D extends AggregateDimension, M extends AggregateMetric> {
    /**
     * 请求时间戳
     */
    long requestTimeStamp;

    D dimension;

    M metric;
}
