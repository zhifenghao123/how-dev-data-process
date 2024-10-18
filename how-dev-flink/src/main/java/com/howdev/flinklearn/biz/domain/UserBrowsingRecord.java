package com.howdev.flinklearn.biz.domain;

import lombok.Data;

@Data
public class UserBrowsingRecord {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品价格
     */
    private Double productPrice;
    /**
     * 浏览时间
     */
    private Long browsingTimestamp;

    public UserBrowsingRecord() {
    }

    public UserBrowsingRecord(String userId, String productName, Double productPrice, Long browsingTimestamp) {
        this.userId = userId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.browsingTimestamp = browsingTimestamp;
    }
}
