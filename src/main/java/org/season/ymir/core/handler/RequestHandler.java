package org.season.ymir.core.handler;

import org.season.ymir.common.base.InvocationType;
import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.model.InvocationMessage;
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
     * @return {@link YmirResponse}
     * @throws Exception
     */
    public InvocationMessage<YmirResponse> handleRequest(YmirRequest data, String requestId) throws RpcException {
        InvocationMessage<YmirResponse> response = new InvocationMessage<>();
        response.setType(InvocationType.SERVICE_RESPONSE);
        // 1.查找服务对应
        ServiceBeanCache bean = serviceRegister.getBean(data.getServiceName());
        if (Objects.isNull(bean)) {
            throw new RpcException("No provider available for service " + data.getServiceName());
        }

        YmirResponse result = null;
        try {
            // 2.反射调用对应的方法过程
            Method method = bean.getClazz().getMethod(data.getMethod(), data.getParameterTypes());
            Object returnValue = method.invoke(bean.getBean(), data.getParameters());
            result = new YmirResponse(ServiceStatusEnum.SUCCESS);
            result.setResult(returnValue);
        } catch (Exception e) {
            result = new YmirResponse(ServiceStatusEnum.ERROR);
            result.setException(e);
        }
        // 3.编组响应消息
        response.setRequestId(requestId);
        response.setBody(result);
        return response;
    }

}
