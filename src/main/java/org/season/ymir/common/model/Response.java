package org.season.ymir.common.model;

import org.season.ymir.common.base.ServiceStatusEnum;

import java.io.Serializable;

/**
 * 返回参数
 *
 * @author KevinClair
 */
public class Response implements Serializable {

    /**
     * 返回结果
     */
    private Object result;

    /**
     * 异常信息
     */
    private Throwable throwable;

    /**
     * 服务状态
     */
    private ServiceStatusEnum statusEnum;

    public Response() {
    }

    public Response(final ServiceStatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

    public Response(final Object result, final Throwable throwable, final ServiceStatusEnum statusEnum) {
        this.result = result;
        this.throwable = throwable;
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
     * Gets the value of throwable.
     *
     * @return the value of throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets the throwable.
     *
     * @param throwable throwable
     */
    public void setThrowable(final Throwable throwable) {
        this.throwable = throwable;
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
