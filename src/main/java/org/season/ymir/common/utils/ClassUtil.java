package org.season.ymir.common.utils;

import java.util.HashMap;

/**
 * 类型解析器
 *
 * @author KevinClair
 **/
public class ClassUtil {

    private static final HashMap<String, Class<?>> primClasses = new HashMap<>();

    static {
        primClasses.put("boolean", boolean.class);
        primClasses.put("Boolean", Boolean.class);
        primClasses.put("byte", byte.class);
        primClasses.put("Byte", Byte.class);
        primClasses.put("char", char.class);
        primClasses.put("Character", Character.class);
        primClasses.put("short", short.class);
        primClasses.put("Short", Short.class);
        primClasses.put("int", int.class);
        primClasses.put("Integer", Integer.class);
        primClasses.put("long", long.class);
        primClasses.put("Long", Long.class);
        primClasses.put("float", float.class);
        primClasses.put("Float", Float.class);
        primClasses.put("double", double.class);
        primClasses.put("Double", Double.class);
        primClasses.put("void", void.class);
        primClasses.put("Void", Void.class);
    }

    /**
     * 类型解析
     *
     * @param className 请求参数类型
     * @return Class类型
     * @throws ClassNotFoundException
     */
    public static Class<?> resolveClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            Class<?> cl = primClasses.get(className);
            if (cl != null) {
                return cl;
            } else {
                throw ex;
            }
        }
    }

}
