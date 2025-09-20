package com.howdev.web.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
public class BaseRequest<T> {
    /**
     * 客户端标识
     */
    @NotBlank(message = "clientId is blank")
    protected String clientId;

    /**
     * 请求时间（时间戳）
     */
    @NotNull(message = "requestTime is null")
    protected Long requestTime;

    /**
     * 请求IP地址
     */
    @NotBlank(message = "ip is blank")
    protected String ip;

    /**
     * 请求业务参数
     */
    @Valid
    @NotNull(message = "data is null")
    protected T data;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
