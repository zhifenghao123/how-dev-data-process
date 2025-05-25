package com.howdev.flinkdev.biz.logstat.domain;

import com.howdev.flinkdev.flink.domain.MyAbstractEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogRecord extends MyAbstractEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private String requestId;
    private long requestTimeStamp;
    private String service;
    private String serviceIp;
    private String method;
    private String returnCode;
    private Long cost;


    // 随机数,处理数据倾斜
    private Integer randomNum;

    public LogRecord() {
    }

    public LogRecord(String requestId, long requestTimeStamp, String serverIp, String service, String method, String returnCode, Long cost) {
        this.requestId = requestId;
        this.requestTimeStamp = requestTimeStamp;
        this.serviceIp = serverIp;
        this.service = service;
        this.method = method;
        this.returnCode = returnCode;
        this.cost = cost;
        this.randomNum = (int) (Math.random() * 10);
    }

    public LogRecord(String requestId, long requestTimeStamp, String serverIp, String service, String method, String returnCode, Long cost, Integer randomNum) {
        this.requestId = requestId;
        this.requestTimeStamp = requestTimeStamp;
        this.serviceIp = serverIp;
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
