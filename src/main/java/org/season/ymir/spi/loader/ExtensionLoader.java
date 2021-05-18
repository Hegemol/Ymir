package org.season.ymir.spi.loader;

import org.apache.commons.lang3.StringUtils;
import org.season.ymir.spi.annodation.SPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加载器
 *
 * @author KevinClair
 **/
public class ExtensionLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String DIRECTORY = "META-INF/ymir/";

    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private final Map<Class<?>, Object> joinInstances = new ConcurrentHashMap<>();

    private final Class<T> clazz;

    private String cachedDefaultName;

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
    public T getLoader(final String name) {
        Holder<Object> objectHolder = cachedInstances.get(name);
        if (objectHolder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            objectHolder = cachedInstances.get(name);
        }
        Object value = objectHolder.getValue();
        if (value == null) {
            synchronized (cachedInstances) {
                if (value == null) {
                    objectHolder.setValue(createExtension(name));
                    cachedInstances.put(name, objectHolder);
                }
            }
        }
        return (T) value;
    }

    private T createExtension(final String name) {
        Class<?> aClass = getExtensionClasses(name).get(name);
        if (aClass == null) {
            throw new IllegalArgumentException("name is error");
        }
        Object o = joinInstances.get(aClass);
        if (o == null) {
            try {
                joinInstances.putIfAbsent(aClass, aClass.newInstance());
                o = joinInstances.get(aClass);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: "
                        + aClass + ")  could not be instantiated: " + e.getMessage(), e);

            }
        }
        return (T) o;
    }

    /**
     * Gets extension classes.
     *
     * @return the extension classes
     */
    public Map<String, Class<?>> getExtensionClasses(final String name) {
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.getValue();
                if (classes == null) {
                    classes = loadExtensionClass(name);
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClass(final String name) {
        SPI annotation = clazz.getAnnotation(SPI.class);
        Map<String, Class<?>> classes = new HashMap<>(16);
        loadDirectory(classes, StringUtils.isBlank(name) ? annotation.value() : name);
        return classes;
    }

    private void loadDirectory(final Map<String, Class<?>> classes, final String name) {
        String fileName = DIRECTORY + clazz.getName();
        try {
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            Enumeration<URL> urls = classLoader != null ? classLoader.getResources(fileName)
                    : ClassLoader.getSystemResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    loadResources(classes, url, name);
                }
            }
        } catch (IOException t) {
            log.error("load extension class error {}", fileName, t);
        }
    }

    private void loadResources(final Map<String, Class<?>> classes, final URL url, final String name) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String classPath = (String)properties.get(name);
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(classPath)) {
                try {
                    loadClass(classes, name, classPath);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("load extension resources error", e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }

    private void loadClass(final Map<String, Class<?>> classes,
                           final String name, final String classPath) throws ClassNotFoundException {
        Class<?> subClass = Class.forName(classPath);
        if (!clazz.isAssignableFrom(subClass)) {
            throw new IllegalStateException("load extension resources error," + subClass + " subtype is not of " + clazz);
        }
        Class<?> oldClass = classes.get(name);
        if (oldClass == null) {
            classes.put(name, subClass);
        } else if (oldClass != subClass) {
            throw new IllegalStateException("load extension resources error,Duplicate class " + clazz.getName() + " name " + name + " on " + oldClass.getName() + " or " + subClass.getName());
        }
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
