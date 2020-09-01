package com.rchen;

import com.rchen.controller.interceptor.MiniInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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
                .addResourceLocations("file:F:/xr-video-mem/");
    }

    /**
     * 容器初始化 zkclient 后，调用 init 方法
     * @return
     */
    @Bean(initMethod = "init")
    public ZKCuratorClient zkCuratorClient() {
        return new ZKCuratorClient();
    }

    /**
     * 拦截器首先注册为 Bean
     * @return
     */
    @Bean
    public MiniInterceptor miniInterceptor() {
        return new MiniInterceptor();
    }

    /**
     * 注册拦截器到注册中心
     * @param registry 注册中心
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(miniInterceptor())
                .addPathPatterns("/user/**")
                .addPathPatterns("/bgm/**")
                .addPathPatterns("/video/upload", "/video/uploadCover",
                        "/video/userLike", "/video/userUnLike",
                        "/video/saveComment")
                .excludePathPatterns("/user/queryPublisher");

        super.addInterceptors(registry);
    }
}
