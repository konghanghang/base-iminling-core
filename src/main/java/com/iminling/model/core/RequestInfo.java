package com.iminling.model.core;

public class RequestInfo {

    private long requestTime;

    private String requestParam;

    public RequestInfo() {
        this(System.currentTimeMillis(), null);
    }

    public RequestInfo(long requestTime, String requestParam) {
        this.requestParam = requestParam;
        this.requestTime = requestTime;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }
}
