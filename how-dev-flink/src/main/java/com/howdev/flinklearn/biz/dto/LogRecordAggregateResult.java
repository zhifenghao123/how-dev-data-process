package com.howdev.flinklearn.biz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogRecordAggregateResult {
    private long requestMinuteTimeStamp;
    private Date requestMinuteTime;
    private String service;
    private String method;

    private Integer count;

    /**
     * 不同响应码的记录数
     */
    private Map<Integer, Integer> codeToCountMap;

    private Long totalTime;
    private Double avgTime;
}
