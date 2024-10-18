package com.howdev.flinklearn.biz.bo;

import com.howdev.flinklearn.biz.domain.OrderRecord;

public class OrderRecordGenerator {
    public static OrderRecord generateOrderRecord(String userId, String productName, Double orderAmount, Long orderTimestamp) {
        return new OrderRecord(userId, productName, orderAmount, orderTimestamp);
    }
}
