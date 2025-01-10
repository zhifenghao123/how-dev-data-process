package com.howdev.flinkdev.log.watermark;

public interface Timeable {
    long getEventTimeStamp();
}
