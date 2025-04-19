package com.animesocial.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 配置WebSocket的端点和消息代理
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置WebSocket端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个STOMP端点，客户端将使用这个端点进行连接
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // 允许所有来源的客户端连接
                .withSockJS();           // 支持SockJS
    }

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理的前缀
        // 客户端发送消息的目标前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 客户端订阅消息的前缀
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 点对点消息前缀
        registry.setUserDestinationPrefix("/user");
    }
} 