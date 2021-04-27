package org.season.ymir.client.annotation;

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

    String value() default "";

    /**
     * 权重
     *
     * @return
     */
    int weight() default 0;
}
