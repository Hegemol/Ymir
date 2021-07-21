package org.season.ymir.common.exception;

/**
 *
 * 超时异常
 *
 * @author KevinClair
 **/
public class RpcTimeoutException extends RpcException{

    public RpcTimeoutException(String message) {
        super(message);
    }
}
