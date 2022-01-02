package org.season.ymir.core.handler;

import org.season.ymir.common.base.InvocationType;
import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.core.filter.DefaultFilterChain;

import java.lang.reflect.Method;
import java.util.Arrays;
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
     * @param msg 请求InvocationMessage
     * @return {@link Response}
     * @throws Exception
     */
    public InvocationMessage<Response> handleRequest(InvocationMessage<Request> msg) throws RpcException {
        Request data = msg.getBody();
        InvocationMessage<Response> response = new InvocationMessage<>();
        response.setType(InvocationType.SERVICE_RESPONSE);
        // 1.查找服务对应
        ServiceBeanCache bean = serviceRegister.getBean(data.getServiceName());
        if (Objects.isNull(bean)) {
            throw new RpcException("No provider available for service " + data.getServiceName());
        }
        // 执行过滤器
        new DefaultFilterChain(new ArrayList<>(Arrays.asList(bean.getFilter().split(","))), CommonConstant.SERVICE_PROVIDER_SIDE).execute(msg.getData());
        Response result;
        try {
            // 2.反射调用对应的方法过程
            Method method = bean.getClazz().getMethod(data.getMethod(), data.getParameterTypes());
            Object returnValue = method.invoke(bean.getBean(), data.getParameters());
            result = new Response(ServiceStatusEnum.SUCCESS);
            result.setResult(returnValue);
        } catch (Exception e) {
            result = new Response(ServiceStatusEnum.ERROR);
            result.setThrowable(e);
        }
        // 3.编组响应消息
        response.setRequestId(msg.getRequestId());
        response.setBody(result);
        return response;
    }

}
