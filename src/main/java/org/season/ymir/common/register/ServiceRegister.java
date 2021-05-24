package org.season.ymir.common.register;

import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;

/**
 * 服务注册接口
 *
 * @author KevinClair
 */
public interface ServiceRegister {

    /**
     * 注册实例
     *
     * @param serviceBean
     * @throws Exception
     */
    void registerBean(final ServiceBean serviceBean) throws Exception;

    /**
     * 获取实例
     *
     * @param name
     * @return
     * @throws Exception
     */
    ServiceBeanCache getBean(final String name) throws Exception;
}
