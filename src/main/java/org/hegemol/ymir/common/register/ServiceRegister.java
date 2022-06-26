package org.hegemol.ymir.common.register;

import org.hegemol.ymir.common.entity.ServiceBean;
import org.hegemol.ymir.common.entity.ServiceBeanCache;
import org.hegemol.ymir.common.exception.RpcException;

/**
 * 服务注册接口
 *
 * @author KevinClair
 */
public interface ServiceRegister {

    /**
     * 注册实例
     *
     * @param serviceBean 需要注册的ServiceBean对象
     * @throws Exception  {@link RpcException}
     */
    void registerBean(final ServiceBean serviceBean) throws RpcException;

    /**
     * 获取实例
     *
     * @param name 服务名
     * @return {@link ServiceBeanCache}
     * @throws Exception {@link RpcException}
     */
    ServiceBeanCache getBean(final String name) throws RpcException;
}
