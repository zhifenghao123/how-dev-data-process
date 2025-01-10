package com.howdev.flinkdev.log.biz.domain;

import com.howdev.flinkdev.log.watermark.Timeable;
import lombok.Data;

import java.io.Serializable;

@Data
public class LogRecord implements Serializable, Timeable {
    private static final long serialVersionUID = 1L;
    private String requestId;
    private long requestTimeStamp;
    private String service;
    private String method;
    private String returnCode;
    private Long cost;


    // 随机数,处理数据倾斜
    private Integer randomNum;

    public LogRecord() {
    }

    public LogRecord(String requestId, long requestTimeStamp, String service, String method, String returnCode, Long cost) {
        this.requestId = requestId;
        this.requestTimeStamp = requestTimeStamp;
        this.service = service;
        this.method = method;
        this.returnCode = returnCode;
        this.cost = cost;

        randomNum = (int) (Math.random() * 20) + 1;
    }

    public LogRecord(String requestId, long requestTimeStamp, String service, String method, String returnCode, Long cost, Integer randomNum) {
        this.requestId = requestId;
        this.requestTimeStamp = requestTimeStamp;
        this.service = service;
        this.method = method;
        this.returnCode = returnCode;
        this.cost = cost;
        this.randomNum = randomNum;
    }

    @Override
    public long getEventTimeStamp() {
        return requestTimeStamp;
    }
}
