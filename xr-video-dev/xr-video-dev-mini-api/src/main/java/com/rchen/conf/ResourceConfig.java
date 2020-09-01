package com.rchen.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author : crz
 */
@Configuration
@ConfigurationProperties(prefix="com.rchen")
@PropertySource("classpath:resource.properties")
public class ResourceConfig {

    private String zookeeperServer;

    private String bgmServer;

    private String filespace;

    private String ffmpeg;

    public String getZookeeperServer() {
        return zookeeperServer;
    }

    public void setZookeeperServer(String zookeeperServer) {
        this.zookeeperServer = zookeeperServer;
    }

    public String getBgmServer() {
        return bgmServer;
    }

    public void setBgmServer(String bgmServer) {
        this.bgmServer = bgmServer;
    }

    public String getFilespace() {
        return filespace;
    }

    public void setFilespace(String filespace) {
        this.filespace = filespace;
    }

    public String getFfmpeg() {
        return ffmpeg;
    }

    public void setFfmpeg(String ffmpeg) {
        this.ffmpeg = ffmpeg;
    }
}
