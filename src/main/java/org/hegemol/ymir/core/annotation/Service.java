package org.hegemol.ymir.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 服务导出
 *
 * @author KevinClair
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface Service {

    /**
     * 权重
     */
    int weight() default 50;

    /**
     * 是否需要注册
     */
    boolean register() default true;

    /**
     * 分组
     */
    String group() default "";

    /**
     * 版本
     */
    String version() default "";

    /**
     * 过滤器
     */
    String filter() default "";

    /**
     * 虚拟节点个数，当负载均衡算法选择一致性hash时，启用
     */
    int fictitiousInstance() default 5;
}
