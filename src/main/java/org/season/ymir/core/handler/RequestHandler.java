package org.season.ymir.core.handler;

import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.register.ServiceRegister;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 服务端请求处理器
 *
 * @author KevinClair
 */
public class RequestHandler {

    private ServiceRegister serviceRegister;

    public RequestHandler(ServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

    /**
     * 请求处理
     *
     * @param data 请求data
     * @return
     * @throws Exception
     */
    public YmirResponse handleRequest(YmirRequest data) throws Exception {
        // 1.查找服务对应
        ServiceBeanCache bean = serviceRegister.getBean(data.getServiceName());
        if (Objects.isNull(bean)) {
            throw new RpcException("No provider for service " + data.getServiceName());
        }

        YmirResponse response = null;
        try {
            // 2.反射调用对应的方法过程
            Method method = bean.getClazz().getMethod(data.getMethod(), data.getParameterTypes());
            Object returnValue = method.invoke(bean.getBean(), data.getParameters());
            response = new YmirResponse(ServiceStatusEnum.SUCCESS);
            response.setReturnValue(returnValue);
        } catch (Exception e) {
            response = new YmirResponse(ServiceStatusEnum.ERROR);
            response.setException(e);
        }
        // 3.编组响应消息
        response.setRequestId(data.getRequestId());
        return response;
    }

}
