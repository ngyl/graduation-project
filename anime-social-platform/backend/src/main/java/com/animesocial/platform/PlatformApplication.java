package com.animesocial.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 动漫社交平台主应用类
 * 
 * 该类是整个应用程序的入口点，使用@SpringBootApplication注解标记，
 * 该注解包含了以下三个注解的功能：
 * 1. @Configuration：标记该类为配置类
 * 2. @EnableAutoConfiguration：启用Spring Boot的自动配置机制
 * 3. @ComponentScan：启用组件扫描，自动发现和注册Bean
 */
@SpringBootApplication
public class PlatformApplication {

    /**
     * 应用程序的主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
    
} 