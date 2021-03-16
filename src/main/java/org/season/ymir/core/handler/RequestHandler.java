package org.season.ymir.core.handler;

import org.season.ymir.common.register.ServiceRegister;

/**
 * 请求处理器
 *
 * @author KevinClair
 */
public class RequestHandler {

    private MessageProtocol protocol;


    private ServiceRegister serviceRegister;

    public RequestHandler(MessageProtocol protocol, ServiceRegister serviceRegister) {
        this.protocol = protocol;
        this.serviceRegister = serviceRegister;
    }


}
