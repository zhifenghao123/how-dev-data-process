package com.howdev.flinklearn.biz.bo;

import com.howdev.flinklearn.biz.domain.UserBrowsingRecord;

public class UserBrowsingRecordGenerator {

    public static UserBrowsingRecord generate(String userId, String productName, Double productPrice, Long browsingTimestamp) {
        return new UserBrowsingRecord(userId, productName, productPrice, browsingTimestamp);
    }

}
