package org.season.ymir.spi.annodation;

import java.lang.annotation.*;

/**
 *
 * 实现类加载
 *
 * @author KevinClair
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Loader {
}
