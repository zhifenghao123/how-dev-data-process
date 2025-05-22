package com.howdev.flinkdev.flink.domain;

public abstract class MyAbstractEvent {
    /**
     * 获取事件时间戳
     * @return 时间戳，单位：毫秒，精确到毫秒，如：15648112811000L，即2019-08-01 11:21:21.000
     */
    public abstract long getEventTimeStamp();
}
