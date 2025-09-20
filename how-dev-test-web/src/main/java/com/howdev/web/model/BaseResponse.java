package com.howdev.web.model;

import org.apache.commons.lang3.StringUtils;

public class BaseResponse<T> {
    /**
     * 响应码
     */
    private String errorCode;
    /**
     * 响应消息
     */
    private String errorMessage;
    /**
     * 业务响应数据
     */
    private T result;

    private static final String SUCC_CODE = "0";
    private static final String SUCC_MSG = "succeed";

    public boolean isSuccess() {
        return StringUtils.equals("0", errorCode);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public static <T> BaseResponse<T> newSuccResponse(T result) {
        return newResponse(SUCC_CODE, SUCC_MSG, result);
    }

    public static <T> BaseResponse<T> newFailResponse(String errorCode, String errorMsg) {
        return newResponse(errorCode, errorMsg, null);
    }

    public static <T> BaseResponse<T> newFailResponse(String errorCode, String errorMsg, T result) {
        return newResponse(errorCode, errorMsg, result);
    }

    private static <T> BaseResponse<T> newResponse(String errorCode, String errorMsg, T result) {
        BaseResponse<T> response = new BaseResponse<T>();
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMsg);
        response.setResult(result);
        return response;
    }

}