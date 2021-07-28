package org.season.ymir.spi;

import org.junit.jupiter.api.Test;
import org.season.ymir.spi.loader.ExtensionLoader;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * SPI测试
 *
 * @author KevinClair
 **/
public class SpiTest {

    @Test
    public void testJavaSpi() {
        Iterator<SpiInterface> iterator = ServiceLoader.load(SpiInterface.class).iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().hello());
        }
    }

    @Test
    public void testYmirSpi() {
        SpiInterface loader = ExtensionLoader.getExtensionLoader(SpiInterface.class).getLoader("");
        System.out.println(loader.hello());

        SpiInterface loaderOne = ExtensionLoader.getExtensionLoader(SpiInterface.class).getLoader("one");
        System.out.println(loaderOne.hello());

        SpiInterface loaderTwo = ExtensionLoader.getExtensionLoader(SpiInterface.class).getLoader("two");
        System.out.println(loaderTwo.hello());
    }
}
