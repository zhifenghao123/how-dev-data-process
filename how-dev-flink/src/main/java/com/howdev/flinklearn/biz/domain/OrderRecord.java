package com.howdev.flinklearn.biz.domain;

import lombok.Data;

@Data
public class OrderRecord {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 订单金额
     */
    private Double orderAmount;
    /**
     * 下单时间
     */
    private Long orderTimestamp;

    public OrderRecord() {
    }

    public OrderRecord(String userId, String productName, Double orderAmount, Long orderTimestamp) {
        this.userId = userId;
        this.productName = productName;
        this.orderAmount = orderAmount;
        this.orderTimestamp = orderTimestamp;
    }


}
