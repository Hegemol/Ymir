package org.season.ymir.common.model;

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
     * 超时时间
     */
    private int timeout;

    /**
     * 重试次数
     */
    private int retries;

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

    public InvocationMessage(final int timeout, final int retries, final Map<String, String> headers, final T body) {
        this.timeout = timeout;
        this.retries = retries;
        this.headers = headers;
        this.body = body;
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
}
