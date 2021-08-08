package org.season.ymir.common.model;

import org.season.ymir.common.base.ServiceStatusEnum;

import java.io.Serializable;

/**
 * 返回参数
 *
 * @author KevinClair
 */
public class YmirResponse implements Serializable {

    /**
     * 返回结果
     */
    private Object result;

    /**
     * 异常信息
     */
    private Exception exception;

    /**
     * 服务状态
     */
    private ServiceStatusEnum statusEnum;

    public YmirResponse() {
    }

    public YmirResponse(final ServiceStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public YmirResponse(final Object result, final Exception exception, final ServiceStatusEnum statusEnum) {
        this.result = result;
        this.exception = exception;
        this.statusEnum = statusEnum;
    }

    /**
     * Gets the value of result.
     *
     * @return the value of result
     */
    public Object getResult() {
        return result;
    }

    /**
     * Sets the result.
     *
     * @param result result
     */
    public void setResult(final Object result) {
        this.result = result;
    }

    /**
     * Gets the value of exception.
     *
     * @return the value of exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Sets the exception.
     *
     * @param exception exception
     */
    public void setException(final Exception exception) {
        this.exception = exception;
    }

    /**
     * Gets the value of statusEnum.
     *
     * @return the value of statusEnum
     */
    public ServiceStatusEnum getStatusEnum() {
        return statusEnum;
    }

    /**
     * Sets the statusEnum.
     *
     * @param statusEnum statusEnum
     */
    public void setStatusEnum(final ServiceStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }
}
