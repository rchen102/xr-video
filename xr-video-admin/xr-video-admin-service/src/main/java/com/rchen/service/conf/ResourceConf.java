package com.rchen.service.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author : crz
 */
@Configuration
@ConfigurationProperties(prefix="com.rchen")
@PropertySource("classpath:resource.properties")
public class ResourceConf {
    private String zookeeperServer;

    private String filespace;

    public String getZookeeperServer() {
        return zookeeperServer;
    }

    public void setZookeeperServer(String zookeeperServer) {
        this.zookeeperServer = zookeeperServer;
    }

    public String getFilespace() {
        return filespace;
    }

    public void setFilespace(String filespace) {
        this.filespace = filespace;
    }
}
