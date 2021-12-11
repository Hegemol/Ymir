package org.season.ymir.common.model;

import org.season.ymir.common.base.InvocationType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理器
 *
 * @author KevinClair
 **/
public class InvocationMessage<T> implements Serializable {

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 重试次数
     */
    private int retries;

    /**
     * 本次消息类型 {@link InvocationType}
     */
    private InvocationType type;

    /**
     * 请求头
     */
    private Map<String,String> headers = new HashMap<>();

    /**
     * 请求内容
     */
    private T body;

    public InvocationMessage() {
    }

    public InvocationMessage(final String requestId, final int timeout, final int retries, final InvocationType type, final T body, final Map<String,String> headers) {
        this.requestId = requestId;
        this.timeout = timeout;
        this.retries = retries;
        this.type = type;
        this.body = body;
        this.headers = headers;
    }

    /**
     * Gets the value of requestId.
     *
     * @return the value of requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the requestId.
     *
     * @param requestId requestId
     */
    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the value of timeout.
     *
     * @return the value of timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout timeout
     */
    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the value of retries.
     *
     * @return the value of retries
     */
    public int getRetries() {
        return retries;
    }

    /**
     * Sets the retries.
     *
     * @param retries retries
     */
    public void setRetries(final int retries) {
        this.retries = retries;
    }

    /**
     * Gets the value of type.
     *
     * @return the value of type
     */
    public InvocationType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type type
     */
    public void setType(final InvocationType type) {
        this.type = type;
    }

    /**
     * Gets the value of body.
     *
     * @return the value of body
     */
    public T getBody() {
        return body;
    }

    /**
     * Sets the body.
     *
     * @param body body
     */
    public void setBody(final T body) {
        this.body = body;
    }

    /**
     * Gets the value of headers.
     *
     * @return the value of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers.
     *
     * @param headers headers
     */
    public void setHeaders(final Map<String, String> headers) {
        this.headers = headers;
    }
}
