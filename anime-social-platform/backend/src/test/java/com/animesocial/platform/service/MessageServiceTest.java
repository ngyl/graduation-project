package com.animesocial.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Message;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.MessageDTO;
import com.animesocial.platform.repository.MessageRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.impl.MessageServiceImpl;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageServiceImpl;

    private MessageService messageService;

    private User testSender;
    private User testReceiver;
    private Message testMessage;
    private MessageDTO testMessageDTO;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        messageService = messageServiceImpl;

        // 初始化测试数据
        testSender = new User();
        testSender.setId(1);
        testSender.setUsername("testsender");

        testReceiver = new User();
        testReceiver.setId(2);
        testReceiver.setUsername("testreceiver");

        testMessage = new Message();
        testMessage.setId(1);
        testMessage.setSenderId(1);
        testMessage.setReceiverId(2);
        testMessage.setContent("测试消息内容");
        testMessage.setSendTime(LocalDateTime.now());
        testMessage.setReadStatus(0);

        testMessageDTO = new MessageDTO();
        testMessageDTO.setId(1);
        testMessageDTO.setSenderId(1);
        testMessageDTO.setReceiverId(2);
        testMessageDTO.setContent("测试消息内容");
        testMessageDTO.setSendTime(LocalDateTime.now());
        testMessageDTO.setReadStatus(0);
    }

    @Test
    @DisplayName("测试发送私信")
    void testSendMessage() {
        // 配置mock行为
        when(userRepository.existsByIdOrUsername(anyInt(), isNull())).thenReturn(true);
        when(messageRepository.insertMessage(any(Message.class))).thenReturn(1);

        // 执行测试
        Message result = messageService.sendMessage(1, 2, "测试消息内容");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getSenderId());
        assertEquals(2, result.getReceiverId());
        assertEquals("测试消息内容", result.getContent());
    }

    @Test
    @DisplayName("测试发送私信给不存在的用户")
    void testSendMessageToNonExistentUser() {
        // 配置mock行为
        when(userRepository.existsByIdOrUsername(anyInt(), isNull())).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            messageService.sendMessage(1, 2, "测试消息内容");
        });
    }

    @Test
    @DisplayName("测试获取收件箱消息")
    void testGetInboxMessages() {
        // 配置mock行为
        when(messageRepository.getInboxMessages(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testMessage));

        // 执行测试
        List<MessageDTO> result = messageService.getInboxMessages(2, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("测试获取发件箱消息")
    void testGetOutboxMessages() {
        // 配置mock行为
        when(messageRepository.getOutboxMessages(anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testMessage));

        // 执行测试
        List<MessageDTO> result = messageService.getOutboxMessages(1, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("测试获取聊天记录")
    void testGetChatHistory() {
        // 配置mock行为
        when(messageRepository.getChatHistory(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(Arrays.asList(testMessage));

        // 执行测试
        List<MessageDTO> result = messageService.getChatHistory(1, 2, 1, 10);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("测试标记消息为已读")
    void testMarkAsRead() {
        // 配置mock行为
        when(messageRepository.getMessageById(anyInt())).thenReturn(testMessage);
        when(messageRepository.markMessageAsRead(anyInt())).thenReturn(1);

        // 执行测试
        messageService.markAsRead(1, 2);

        // 验证结果
        verify(messageRepository).markMessageAsRead(anyInt());
    }

    @Test
    @DisplayName("测试标记不存在的消息为已读")
    void testMarkNonExistentMessageAsRead() {
        // 配置mock行为
        when(messageRepository.getMessageById(anyInt())).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            messageService.markAsRead(1, 2);
        });
    }

    @Test
    @DisplayName("测试批量标记消息为已读")
    void testBatchMarkAsRead() {
        // 配置mock行为
        when(messageRepository.batchMarkAsRead(anyInt(), anyInt())).thenReturn(5);

        // 执行测试
        int result = messageService.batchMarkAsRead(2, 1);

        // 验证结果
        assertEquals(5, result);
    }

    @Test
    @DisplayName("测试删除消息")
    void testDeleteMessage() {
        // 配置mock行为
        when(messageRepository.getMessageById(anyInt())).thenReturn(testMessage);
        when(messageRepository.deleteMessage(anyInt())).thenReturn(1);

        // 执行测试
        messageService.deleteMessage(1, 1);

        // 验证结果
        verify(messageRepository).deleteMessage(anyInt());
    }

    @Test
    @DisplayName("测试删除不存在的消息")
    void testDeleteNonExistentMessage() {
        // 配置mock行为
        when(messageRepository.getMessageById(anyInt())).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            messageService.deleteMessage(1, 1);
        });
    }

    @Test
    @DisplayName("测试获取聊天好友列表")
    void testGetChatFriends() {
        // 配置mock行为
        when(messageRepository.getLatestMessagesWithUsers(anyInt())).thenReturn(Arrays.asList(testMessage));

        // 执行测试
        List<MessageDTO> result = messageService.getChatFriends(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("测试获取未读消息数量")
    void testGetUnreadCount() {
        // 配置mock行为
        when(messageRepository.getUnreadCount(anyInt())).thenReturn(5);

        // 执行测试
        int result = messageService.getUnreadCount(2);

        // 验证结果
        assertEquals(5, result);
    }

    @Test
    @DisplayName("测试获取特定发送者的未读消息数量")
    void testGetUnreadCountFromSender() {
        // 配置mock行为
        when(messageRepository.getUnreadCountFromSender(anyInt(), anyInt())).thenReturn(3);

        // 执行测试
        int result = messageService.getUnreadCountFromSender(2, 1);

        // 验证结果
        assertEquals(3, result);
    }
} 