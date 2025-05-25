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
public class AggregateMetric {
    /**
     * 请求总数
     */
    Long totalCount;

    // -------以下为当前分钟内，不同秒的qps---------
    /**
     * 不同秒的qps
     */
    Map<Long, Long> secondToCountMap;
    /**
     * 最大qps
     */
    Long maxQps;
}
