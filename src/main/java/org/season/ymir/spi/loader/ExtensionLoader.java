package org.season.ymir.spi.loader;

import org.season.ymir.spi.annodation.SPI;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载器
 *
 * @author KevinClair
 **/
public class ExtensionLoader<T> {

    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    public ExtensionLoader(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 根据对应的class对象获取指定的ExtensionLoader
     *
     * @param clazz 类对象
     * @param <T>   泛型
     * @return {@link ExtensionLoader}
     */
    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        if (Objects.isNull(clazz)) {
            throw new NullPointerException("extension clazz is null");
        }
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") is not interface!");
        }
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") without @" + SPI.class + " Annotation");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (Objects.nonNull(extensionLoader)) {
            return extensionLoader;
        }
        LOADERS.putIfAbsent(clazz, new ExtensionLoader<>(clazz));
        return (ExtensionLoader<T>) LOADERS.get(clazz);
    }

    /**
     * 获取name对应的实现类
     *
     * @param name 具体的SPI对应的名称
     * @return
     */
    public T getLoader(final String name){
        Object value = cachedInstances.get(name);
        if (value == null) {
            synchronized (cachedInstances) {
                if (value == null) {
                    // TODO 获取对应实例
//                    value = createExtension(name);
                }
            }
        }
        return (T) value;
    }

    /**
     * The type Holder.
     *
     * @param <T> the type parameter.
     */
    public static class Holder<T> {

        private volatile T value;

        /**
         * Gets value.
         *
         * @return the value
         */
        public T getValue() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(final T value) {
            this.value = value;
        }
    }
}
