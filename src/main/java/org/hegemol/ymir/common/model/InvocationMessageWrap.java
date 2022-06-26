package org.hegemol.ymir.common.model;

import org.hegemol.ymir.common.base.MessageTypeEnum;
import org.hegemol.ymir.common.base.SerializationTypeEnum;

import java.io.Serializable;

/**
 * TODO
 *
 * @author KevinClair
 **/
public class InvocationMessageWrap<T> implements Serializable {

    /**
     * 请求id
     */
    private int requestId;

    /**
     * 本次消息类型 {@link MessageTypeEnum}
     */
    private MessageTypeEnum type;

    /**
     * 序列化类型
     */
    private SerializationTypeEnum serial;

    /**
     * 数据
     */
    private InvocationMessage<T> data;

    public InvocationMessageWrap() {
    }

    public InvocationMessageWrap(final int requestId, final MessageTypeEnum type, final SerializationTypeEnum serial, final InvocationMessage<T> data) {
        this.requestId = requestId;
        this.type = type;
        this.serial = serial;
        this.data = data;
    }

    /**
     * Gets the value of requestId.
     *
     * @return the value of requestId
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Sets the requestId.
     *
     * @param requestId requestId
     */
    public void setRequestId(final int requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the value of type.
     *
     * @return the value of type
     */
    public MessageTypeEnum getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type type
     */
    public void setType(final MessageTypeEnum type) {
        this.type = type;
    }

    /**
     * Gets the value of serial.
     *
     * @return the value of serial
     */
    public SerializationTypeEnum getSerial() {
        return serial;
    }

    /**
     * Sets the serial.
     *
     * @param serial serial
     */
    public void setSerial(final SerializationTypeEnum serial) {
        this.serial = serial;
    }

    /**
     * Gets the value of data.
     *
     * @return the value of data
     */
    public InvocationMessage<T> getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data data
     */
    public void setData(final InvocationMessage<T> data) {
        this.data = data;
    }
}
