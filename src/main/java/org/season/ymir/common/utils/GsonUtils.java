package org.season.ymir.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Gson utils
 *
 * @author KevinClair
 */
public class GsonUtils {

    private static final String EMPTY = "";

    private static final GsonUtils INSTANCE = new GsonUtils();

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(String.class, new StringTypeAdapter())
            .create();

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

    private static class StringTypeAdapter extends TypeAdapter<String> {
        @Override
        public void write(final JsonWriter out, final String value) throws IOException {
            if (StringUtils.isBlank(value)) {
                out.nullValue();
                return;
            }
            out.value(value);
        }

        @Override
        public String read(final JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return EMPTY;
            }
            return reader.nextString();
        }
    }
}
