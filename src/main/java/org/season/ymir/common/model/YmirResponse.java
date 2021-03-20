package org.season.ymir.common.model;

import org.season.ymir.common.base.ServiceStatusEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回参数
 *
 * @author KevinClair
 */
public class YmirResponse implements Serializable {

    private String requestId;

    private Map<String, String> headers = new HashMap<>();

    private Object returnValue;

    private Exception exception;

    private ServiceStatusEnum statusEnum;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public ServiceStatusEnum getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(ServiceStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public YmirResponse(ServiceStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }
}
