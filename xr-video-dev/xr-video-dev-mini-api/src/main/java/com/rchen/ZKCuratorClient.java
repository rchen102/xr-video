package com.rchen;

import com.rchen.conf.ResourceConfig;
import com.rchen.enums.BGMOperatorTypeEnum;
import com.rchen.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;


/**
 * Zookeeper 配置类
 *
 * @Author : crz
 */
@Component
public class ZKCuratorClient {

    // zk 客户端
    private CuratorFramework client = null;

    @Autowired
    private ResourceConfig resourceConfig;

    public void init() {
        if (client != null) {
            return;
        }
        // 1. 创建重连策略：最多重试 5 次，每次重连的等待时间 1 秒
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);

        // 2. 创建客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(resourceConfig.getZookeeperServer())
                .sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy)
                .namespace("admin")
                .build();

        // 3. 启动客户端
        client.start();

        try {
            addChildWatch("/bgm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听节点，只需要执行一次，之后持续监听
     * @param nodePath 需要监听的节点路径（namespace 下路径）
     */
    public void addChildWatch(String nodePath) throws Exception {
        // 通过缓存 node state，实现监听
        final PathChildrenCache cache = new PathChildrenCache(client, nodePath, true);
        cache.start();
        // 2. 添加监听器
        // 获取当前监听器列表
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(Type.CHILD_ADDED)) {
                    // 1. 从 zknode 中获取相对路径和操作类型
                    String zknodePath = event.getData().getPath();
                    String zknodeData = new String(event.getData().getData());
                    Map<String, String> payload = JsonUtils.jsonToPojo(zknodeData, Map.class);

                    String opType = payload.get("op");
                    String bgmPath = payload.get("path");

                    // 2. BGM本地存储路径
                    String finalPath = resourceConfig.getFilespace() + bgmPath;

                    // 3. 判断操作类型
                    if (opType.equals(BGMOperatorTypeEnum.ADD.type)) {
                        // 定义下载的路径（播放的url）
                        String downloadPath = resourceConfig.getBgmServer();
                        String[] pathArr = bgmPath.split("/");
                        for (int i = 0; i < pathArr.length; i++) {
                            if (StringUtils.isNotBlank(pathArr[i])) {
                                downloadPath += "/";
                                downloadPath += URLEncoder.encode(pathArr[i], "UTF-8");
                            }
                        }
                        // 下载到小程序服务端
                        URL url = new URL(downloadPath);
                        File file = new File(finalPath);
                        FileUtils.copyURLToFile(url, file);
                        // 删除zknode
                        client.delete().forPath(zknodePath);
                    } else if (opType.equals(BGMOperatorTypeEnum.DELETE.type)) {
                        // 删除文件
                        File file = new File(finalPath);
                        FileUtils.forceDelete(file);
                        // 删除zknode
                        client.delete().forPath(zknodePath);
                    }
                }
            }
        });
    }
}
