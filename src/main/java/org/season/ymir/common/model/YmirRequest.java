package org.season.ymir.common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求参数
 *
 * @author KevinClair
 */
public class YmirRequest implements Serializable {

    private String requestId;
    /**
     * 请求的服务名
     */
    private String serviceName;
    /**
     * 请求调用的方法
     */
    private String method;

    private Map<String,String> headers = new HashMap<>();

    private Class<?>[] parameterTypes;

    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
