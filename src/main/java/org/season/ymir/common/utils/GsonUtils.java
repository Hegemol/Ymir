package org.season.ymir.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Gson utils
 *
 * @author KevinClair
 */
public class GsonUtils {

    private static final GsonUtils INSTANCE = new GsonUtils();

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static GsonUtils getInstance() {
        return INSTANCE;
    }

    /**
     * To json string.
     *
     * @param object the object
     * @return the string
     */
    public String toJson(final Object object) {
        return GSON.toJson(object);
    }

    /**
     * From json t.
     *
     * @param <T>    the type parameter
     * @param json   the json
     * @param tClass the t class
     * @return the t
     */
    public <T> T fromJson(final String json, final Class<T> tClass) {
        return GSON.fromJson(json, tClass);
    }
}
