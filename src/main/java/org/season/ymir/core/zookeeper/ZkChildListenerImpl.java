package org.season.ymir.core.zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.season.ymir.core.cache.YmirServerDiscoveryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TODO
 *
 * @author KevinClair
 **/
public class ZkChildListenerImpl implements IZkChildListener {

    private static Logger logger = LoggerFactory.getLogger(ZkChildListenerImpl.class);

    /**
     * 监听子节点的删除和新增事件
     *
     * @param parentPath /rpc/serviceName/service
     * @param childList
     * @throws Exception
     */
    @Override
    public void handleChildChange(String parentPath, List<String> childList) throws Exception {
        logger.debug("Child change parentPath:[{}] -- childList:[{}]", parentPath, childList);
        // 只要子节点有改动就清空缓存
        String[] arr = parentPath.split("/");
        YmirServerDiscoveryCache.remove(arr[2]);
    }
}
