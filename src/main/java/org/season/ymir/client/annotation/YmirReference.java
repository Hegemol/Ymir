package org.season.ymir.client.annotation;

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

    String value() default "";
}
