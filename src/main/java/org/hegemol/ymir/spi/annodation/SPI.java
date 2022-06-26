package org.hegemol.ymir.spi.annodation;

import java.lang.annotation.*;

/**
 * SPI注解
 *
 * @author KevinClair
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {

    /**
     * Value string.
     *
     * @return the string
     */
    String value();
}
