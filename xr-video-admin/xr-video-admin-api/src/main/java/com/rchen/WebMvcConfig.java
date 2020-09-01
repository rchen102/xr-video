package com.rchen;

import com.rchen.util.ZKCuratorClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @Author : crz
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /**
         * </**> 表示可以访问（目录下）所有资源
         * Locations 配置目录所在位置
         */
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("file:F:/xr-video-admin-mem/");
    }

    @Bean(initMethod = "init")
    public ZKCuratorClient zkCuratorClient() {
        return new ZKCuratorClient();
    }
}
