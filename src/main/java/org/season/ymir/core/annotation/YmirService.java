package org.season.ymir.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务导出
 *
 * @author KevinClair
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface YmirService {

    /**
     * 权重
     *
     * @return
     */
    int weight() default 0;

    /**
     * 是否需要注册
     *
     * @return
     */
    boolean register() default true;

    /**
     * 分组
     *
     * @return
     */
    String group() default "";

    /**
     * 版本
     *
     * @return
     */
    String version() default "";
}
