package org.season.ymir.common.utils;

import org.season.ymir.common.constant.CommonConstant;

/**
 * zookeeper的node节点路径
 *
 * @author KevinClair
 */
public class ZkPathUtils {

    /**
     * 生成zk节点路径
     *
     * @param side provider or consumer
     * @param name 接口名
     * @return
     */
    public static String buildPath(String side, String name) {
        return String.join(CommonConstant.PATH_DELIMITER, CommonConstant.PATH_DELIMITER + name, side);
    }

    public static String buildUriPath(String nodePath, String uri) {
        return String.join(CommonConstant.PATH_DELIMITER, nodePath, uri);
    }
}
