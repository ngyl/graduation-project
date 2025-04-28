package com.animesocial.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 
 * 注意：WebSocket使用不同于HTTP的协议和路径规范
 * - WebSocket端点与RESTful API路径分开，避免冲突
 * - WebSocket使用STOMP子协议进行消息传递，而不是HTTP请求/响应
 * - 消息格式和路由方式也完全不同
 * 
 * 路径说明：
 * - /ws：WebSocket连接端点（相当于HTTP的基础URL）
 * - /app：客户端发送消息的目标前缀（处理输入）
 * - /topic：广播消息的目标前缀（一对多）
 * - /queue：点对点消息的目标前缀（一对一）
 * - /user：用户特定消息的前缀
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置WebSocket端点
     * 这是客户端连接到WebSocket服务器的入口
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个STOMP端点，客户端将使用这个端点进行连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:4173", "http://127.0.0.1:5173")  // 允许前端开发服务器地址
                .withSockJS();           // 支持SockJS
    }

    /**
     * 配置消息代理
     * 定义消息的路由规则和转发行为
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置应用目标前缀
        // 客户端发送消息的目标前缀，对应@MessageMapping注解处理的路径
        registry.setApplicationDestinationPrefixes("/app");
        
        // 配置消息代理的前缀
        // 客户端订阅这些前缀的目标来接收消息
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 点对点消息前缀
        // 用于向特定用户发送消息
        registry.setUserDestinationPrefix("/user");
    }
} 