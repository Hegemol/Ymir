package org.season.ymir.client.process;

import org.season.ymir.client.annotation.YmirService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Objects;
import java.util.Optional;

/**
 * 服务导出
 *
 * @author KevinClair
 */
public class DefaultServiceExportProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        YmirService annotation = bean.getClass().getAnnotation(YmirService.class);
        if (Objects.isNull(annotation)) return bean;

        return null;
    }
}
