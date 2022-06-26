package org.hegemol.ymir.core.event;


import org.springframework.context.ApplicationEvent;

/**
 * 服务导出事件
 *
 * @author KevinClair
 */
public class ServiceBeanExportEvent extends ApplicationEvent {

    public ServiceBeanExportEvent(Object source) {
        super(source);
    }
}
