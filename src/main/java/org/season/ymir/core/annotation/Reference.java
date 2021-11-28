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
public @interface Reference {

    /**
     * 检查服务状态
     *
     * @return
     */
    boolean check() default false;

    /**
     * 负载均衡
     */
    String loadBalance() default "random";

    /**
     * 超时时间
     */
    int timeout() default 3000;

    /**
     * 重试次数
     */
    int retries() default 2;

    /**
     * 服务直连url
     */
    String url() default "";

    /**
     * 过滤器
     */
    String filter() default "";
}
