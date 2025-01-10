package com.howdev.common.example.domain;

import java.io.Serializable;

public class LogRecordExample implements Serializable {
    private static final long serialVersionUID = 1L;
    private String requestId;
    private long requestTimeStamp;
    private String service;
    private String method;
    private String returnCode;
    private Long cost;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(long requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public Long getCost() {
        return cost;
    }

    public void setCost(Long cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "LogRecord{" +
                "requestId='" + requestId + '\'' +
                ", requestTimeStamp=" + requestTimeStamp +
                ", service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", returnCode='" + returnCode + '\'' +
                ", cost=" + cost +
                '}';
    }
}
