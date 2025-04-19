package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.MessageDTO;
import com.animesocial.platform.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 处理与消息相关的HTTP请求，包括：
 * 1. 消息查询和管理
 * 2. 聊天记录获取
 * 3. 未读消息处理
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private MessageService messageService;

    /**
     * 获取收件箱消息
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 收件箱消息列表
     */
    @GetMapping("/inbox")
    public ApiResponse<List<MessageDTO>> getInboxMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(messageService.getInboxMessages(userId, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取发件箱消息
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 发件箱消息列表
     */
    @GetMapping("/outbox")
    public ApiResponse<List<MessageDTO>> getOutboxMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(messageService.getOutboxMessages(userId, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取与指定用户的聊天记录
     * @param friendId 好友用户ID
     * @param page 页码
     * @param size 每页数量
     * @param session HTTP会话
     * @return 聊天记录列表
     */
    @GetMapping("/chat/{friendId}")
    public ApiResponse<List<MessageDTO>> getChatHistory(
            @PathVariable Integer friendId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 获取聊天记录的同时，标记来自好友的消息为已读
            messageService.batchMarkAsRead(userId, friendId);
            
            return ApiResponse.success(messageService.getChatHistory(userId, friendId, page, size));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 发送消息
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/send")
    public ApiResponse<Void> sendMessage(
            @RequestParam Integer receiverId,
            @RequestParam String content,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            messageService.sendMessage(userId, receiverId, content);
            return ApiResponse.success("消息发送成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取聊天好友列表
     * @param session HTTP会话
     * @return 聊天好友列表，每个好友附带最新一条消息
     */
    @GetMapping("/chat-friends")
    public ApiResponse<List<MessageDTO>> getChatFriends(HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(messageService.getChatFriends(userId));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Integer messageId,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            messageService.markAsRead(messageId, userId);
            return ApiResponse.success("消息已标记为已读", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 批量标记来自特定用户的消息为已读
     * @param senderId 发送者ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public ApiResponse<Integer> batchMarkAsRead(
            @RequestParam Integer senderId,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            int count = messageService.batchMarkAsRead(userId, senderId);
            return ApiResponse.success("已标记" + count + "条消息为已读", count);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 删除消息
     * @param messageId 消息ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable Integer messageId,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            messageService.deleteMessage(messageId, userId);
            return ApiResponse.success("消息已删除", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取用户未读消息数量
     * @param session HTTP会话
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(HttpSession session) {
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(messageService.getUnreadCount(userId));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 