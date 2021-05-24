package org.season.ymir.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 服务注入
 *
 * @author KevinClair
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface YmirReference {

    /**
     * value
     *
     * @return
     */
    String value() default "";

    /**
     * 检查服务状态
     *
     * @return
     */
    boolean check() default false;
}
