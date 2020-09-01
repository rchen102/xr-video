package com.rchen.util;

import com.rchen.service.conf.ResourceConf;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Zookeeper 配置类
 *
 * @Author : crz
 */
public class ZKCuratorClient {
    // zk 客户端
    private CuratorFramework client = null;

    @Autowired
    private ResourceConf resourceConf;

    public void init() {
        if (client != null) {
            return;
        }
        // 1. 创建重连策略：最多重试 5 次，每次重连的等待时间 1 秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);

        // 2. 创建客户端 会话超时时间 10s
        client = CuratorFrameworkFactory.builder()
                .connectString(resourceConf.getZookeeperServer())
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                .namespace("admin")
                .build();

        // 3. 启动客户端
        client.start();

        try {
            // 判断 admin 命名空间下，是否有 bgm 节点 /admin/bgm
            if (client.checkExists().forPath("/bgm") == null) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(Ids.OPEN_ACL_UNSAFE)
                        .forPath("/bgm");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加或者删除BGM，向zk-server创建子节点，供小程序监听
     * @param bgmId
     * @param operatorType
     */
    public void sendBgmOperator(String bgmId, String operatorType) {
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .withACL(Ids.OPEN_ACL_UNSAFE)
                    .forPath("/bgm/" + bgmId, operatorType.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
