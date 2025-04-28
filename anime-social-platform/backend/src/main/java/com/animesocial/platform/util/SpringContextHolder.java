package com.animesocial.platform.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文持有者
 * 用于在非Spring管理的类中获取Spring容器中的Bean
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    /**
     * 获取Bean
     * @param <T> Bean类型
     * @param requiredType Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> requiredType) {
        if (applicationContext == null) {
            throw new IllegalStateException("applicationContext未初始化");
        }
        return applicationContext.getBean(requiredType);
    }

    /**
     * 根据名称获取Bean
     * @param <T> Bean类型
     * @param name Bean名称
     * @param requiredType Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        if (applicationContext == null) {
            throw new IllegalStateException("applicationContext未初始化");
        }
        return applicationContext.getBean(name, requiredType);
    }
} 