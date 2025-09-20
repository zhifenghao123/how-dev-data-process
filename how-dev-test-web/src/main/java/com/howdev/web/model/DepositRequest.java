package com.howdev.web.model;

import lombok.Data;

@Data
public class DepositRequest {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 存款金额,单位分
     */
    private Long amount;
}
