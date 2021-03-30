package org.season.ymir.common.exception;

/**
 * Rpc异常
 *
 * @author KevinClair
 **/
public class RpcException extends RuntimeException{

    public RpcException(String message) {
        super(message);
    }
}
