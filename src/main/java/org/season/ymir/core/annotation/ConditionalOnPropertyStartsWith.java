package org.season.ymir.core.annotation;

import org.season.ymir.core.handler.OnPropertyStartsWithCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 *
 * @author KevinClair
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPropertyStartsWithCondition.class)
public @interface ConditionalOnPropertyStartsWith {

    String[] value() default {};

    String prefix() default "";

    String[] name() default {};

    String havingStartsWithValue() default "";

    boolean matchIfMissing() default false;
}
