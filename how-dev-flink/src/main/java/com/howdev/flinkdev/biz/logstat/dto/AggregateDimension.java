package com.howdev.flinkdev.biz.logstat.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AggregateDimension {
    private String service;
    private String serviceIp;
    private String method;
    private String returnCode;
}
