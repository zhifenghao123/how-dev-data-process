package com.howdev.web.model;

import lombok.Data;

import java.util.List;

@Data
public class BatchDepositRequest {
    /**
     * 批次号
     */
    private String processBatchNo;
    /**
     * 多个用户的存款请求
     */
    private List<DepositRequest> depositRequests;
}
