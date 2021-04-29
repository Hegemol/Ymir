package org.season.ymir.core.handler;

import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.core.protocol.MessageProtocol;

import java.lang.reflect.Method;
import java.util.Objects;

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

    /**
     * 请求处理
     *
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] handleRequest(byte[] data) throws Exception {
        // 1.解组消息
        YmirRequest req = this.protocol.unmarshallingRequest(data);

        // 2.查找服务对应
        ServiceBeanCache bean = serviceRegister.getBean(req.getServiceName());

        YmirResponse response = null;

        if (Objects.isNull(bean)) {
            response = new YmirResponse(ServiceStatusEnum.NOT_FOUND);
        } else {
            try {
                // 3.反射调用对应的方法过程
                Method method = bean.getClazz().getMethod(req.getMethod(), req.getParameterTypes());
                Object returnValue = method.invoke(bean.getBean(), req.getParameters());
                response = new YmirResponse(ServiceStatusEnum.SUCCESS);
                response.setReturnValue(returnValue);
            } catch (Exception e) {
                response = new YmirResponse(ServiceStatusEnum.ERROR);
                response.setException(e);
            }
        }
        // 编组响应消息
        response.setRequestId(req.getRequestId());
        return this.protocol.marshallingResponse(response);
    }

}
