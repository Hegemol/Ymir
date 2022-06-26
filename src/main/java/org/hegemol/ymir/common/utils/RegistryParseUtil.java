package org.hegemol.ymir.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册地址解析工具类
 *
 * @author KevinClair
 **/
public class RegistryParseUtil {

    /**
     * 解析url中的地址信息
     *
     * @param config       url配置信息
     * @param registryType 注册类型
     * @return 当前注册类型的地址
     */
    public static String parseAddress(String config, String registryType) {
        String address = null;

        if (StringUtils.isNotEmpty(config) && config.startsWith(registryType)) {
            final String registryTypeString = registryType + "://";
            String value = config.substring(registryTypeString.length());
            if (!value.contains("?")) {
                address = value;
            } else {
                int index = value.lastIndexOf('?');
                address = value.substring(0, index);
            }
        }

        return address;
    }

    /**
     * 解析url中的配置参数信息
     *
     * @param config       url配置信息
     * @param registryType 注册类型
     * @return 配置参数，key为配置key，value为配置的值
     */
    public static Map<String, String> parseParam(String config, String registryType) {

        String address = parseAddress(config, registryType);

        String paramString = config.substring(config.indexOf(address) + address.length());

        if (StringUtils.isNotEmpty(paramString) && paramString.startsWith("?")) {
            paramString = paramString.substring(1);
        }

        Map<String, String> map = new HashMap<String, String>();
        if (paramString.contains("&")) {
            String[] paramSplit = paramString.split("&");
            for (String param : paramSplit) {
                Map<String, String> tempMap = parseKeyValue(param);
                map.putAll(tempMap);
            }
        } else {
            Map<String, String> tempMap = parseKeyValue(paramString);
            map.putAll(tempMap);
        }
        map.put("address", address);
        return map;
    }

    private static Map<String, String> parseKeyValue(String kv) {
        Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(kv)) {
            String[] kvSplit = kv.split("=");
            String key = kvSplit[0];
            String value = kvSplit[1];
            map.put(key, value);
        }
        return map;
    }
}
