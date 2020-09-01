package com.rchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author : crz
 */
@SpringBootApplication
@MapperScan(basePackages = "com.rchen.mapper")
@ComponentScan(basePackages = {"com.rchen", "org.n3r.idworker"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
