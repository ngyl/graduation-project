package com.animesocial.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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

import com.animesocial.platform.model.Event;
import com.animesocial.platform.model.EventParticipation;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.EventParticipationDTO;
import com.animesocial.platform.repository.EventParticipationRepository;
import com.animesocial.platform.repository.EventRepository;
import com.animesocial.platform.service.impl.EventServiceImpl;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipationRepository eventParticipationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventServiceImpl eventServiceImpl;

    private EventService eventService;

    private Event testEvent;
    private EventDTO testEventDTO;
    private CreateEventRequest testCreateRequest;
    private EventParticipation testParticipation;
    private EventParticipationDTO testParticipationDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 将实现类赋值给接口变量
        eventService = eventServiceImpl;

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testEvent = new Event();
        testEvent.setId(1);
        testEvent.setTitle("测试活动");
        testEvent.setDescription("测试活动描述");
        testEvent.setStartTime(LocalDate.now());
        testEvent.setEndTime(LocalDate.now().plusDays(7));
        testEvent.setStatus(1);
        testEvent.setCreatedAt(LocalDateTime.now());

        testEventDTO = new EventDTO();
        testEventDTO.setId(1);
        testEventDTO.setTitle("测试活动");
        testEventDTO.setDescription("测试活动描述");
        testEventDTO.setStartTime(LocalDate.now());
        testEventDTO.setEndTime(LocalDate.now().plusDays(7));
        testEventDTO.setStatus(1);

        testCreateRequest = new CreateEventRequest();
        testCreateRequest.setTitle("测试活动");
        testCreateRequest.setDescription("测试活动描述");
        testCreateRequest.setStartTime(LocalDate.now());
        testCreateRequest.setEndTime(LocalDate.now().plusDays(7));

        testParticipation = new EventParticipation();
        testParticipation.setId(1);
        testParticipation.setEventId(1);
        testParticipation.setUserId(1);
        testParticipation.setParticipationTime(LocalDateTime.now());
        testParticipation.setStatus(1);

        testParticipationDTO = new EventParticipationDTO();
        testParticipationDTO.setId(1);
        testParticipationDTO.setEventId(1);
        testParticipationDTO.setUserId(1);
    }

    @Test
    @DisplayName("测试根据ID获取活动")
    void testGetEventById() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);

        // 执行测试
        EventDTO result = eventService.getEventById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testEvent.getTitle(), result.getTitle());
    }

    @Test
    @DisplayName("测试获取所有活动")
    void testGetAllEvents() {
        // 配置mock行为
        when(eventRepository.findAllByFilter(anyInt(), any(), any())).thenReturn(Arrays.asList(testEvent));

        // 执行测试
        List<EventDTO> result = eventService.getAllEvents(1, LocalDate.now(), LocalDate.now().plusDays(7));

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("测试创建活动")
    void testCreateEvent() {
        // 配置mock行为
        doNothing().when(eventRepository).insert(any(Event.class));

        // 执行测试
        EventDTO result = eventService.createEvent(1, testCreateRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testCreateRequest.getTitle(), result.getTitle());
        verify(eventRepository).insert(any(Event.class));
    }

    @Test
    @DisplayName("测试更新活动")
    void testUpdateEvent() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        doNothing().when(eventRepository).update(any(Event.class));

        // 执行测试
        EventDTO result = eventService.updateEvent(1, testCreateRequest);

        // 验证结果
        assertNotNull(result);
        assertEquals(testCreateRequest.getTitle(), result.getTitle());
        verify(eventRepository).update(any(Event.class));
    }

    @Test
    @DisplayName("测试更新活动状态")
    void testUpdateEventStatus() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        doNothing().when(eventRepository).updateStatus(anyInt(), anyInt());

        // 执行测试
        assertDoesNotThrow(() -> eventService.updateEventStatus(1, 0));

        // 验证mock调用
        verify(eventRepository).updateStatus(1, 0);
    }

    @Test
    @DisplayName("测试删除活动")
    void testDeleteEvent() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        doNothing().when(eventRepository).delete(anyInt());

        // 执行测试
        assertDoesNotThrow(() -> eventService.deleteEvent(1));

        // 验证mock调用
        verify(eventRepository).delete(1);
    }

    @Test
    @DisplayName("测试获取当前进行中的活动")
    void testGetCurrentEvents() {
        // 配置mock行为
        when(eventRepository.findCurrentEvents(any(LocalDate.class))).thenReturn(Arrays.asList(testEvent));

        // 执行测试
        List<EventDTO> result = eventService.getCurrentEvents();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("测试获取即将开始的活动")
    void testGetUpcomingEvents() {
        // 配置mock行为
        when(eventRepository.findUpcomingEvents(any(LocalDate.class), any(LocalDate.class))).thenReturn(Arrays.asList(testEvent));

        // 执行测试
        List<EventDTO> result = eventService.getUpcomingEvents();

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEvent.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("测试用户参加活动")
    void testParticipateEvent() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        when(eventParticipationRepository.exists(anyInt(), anyInt())).thenReturn(false);
        doNothing().when(eventParticipationRepository).insert(any(EventParticipation.class));

        // 执行测试
        assertDoesNotThrow(() -> eventService.participateEvent(1, 1));

        // 验证mock调用
        verify(eventParticipationRepository).insert(any(EventParticipation.class));
    }

    @Test
    @DisplayName("测试用户取消参加活动")
    void testCancelParticipation() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        when(eventParticipationRepository.exists(anyInt(), anyInt())).thenReturn(true);
        doNothing().when(eventParticipationRepository).delete(anyInt(), anyInt());

        // 执行测试
        assertDoesNotThrow(() -> eventService.cancelParticipation(1, 1));

        // 验证mock调用
        verify(eventParticipationRepository).delete(1, 1);
    }

    @Test
    @DisplayName("测试检查用户是否参加了活动")
    void testCheckParticipation() {
        // 配置mock行为
        when(eventParticipationRepository.exists(anyInt(), anyInt())).thenReturn(true);

        // 执行测试
        boolean result = eventService.checkParticipation(1, 1);

        // 验证结果
        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查活动是否已结束")
    void testIsEventEnded() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);

        // 执行测试
        boolean result = eventService.isEventEnded(1);

        // 验证结果
        assertFalse(result);
    }

    @Test
    @DisplayName("测试获取活动总数")
    void testCountEvents() {
        // 配置mock行为
        when(eventRepository.count()).thenReturn(1);

        // 执行测试
        int result = eventService.countEvents(1);

        // 验证结果
        assertEquals(1, result);
    }

    @Test
    @DisplayName("测试获取活动的参与者列表")
    void testGetEventParticipants() {
        // 配置mock行为
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        when(eventParticipationRepository.findUserIdsByEventId(anyInt())).thenReturn(Arrays.asList(1));
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        List<EventParticipationDTO> result = eventService.getEventParticipants(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("测试获取用户参与的活动记录")
    void testGetUserParticipations() {
        // 配置mock行为
        when(eventParticipationRepository.findEventIdsByUserId(anyInt())).thenReturn(Arrays.asList(1));
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        List<EventParticipationDTO> result = eventService.getUserParticipations(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("测试根据ID获取活动参与记录")
    void testGetParticipationById() {
        // 配置mock行为
        when(eventParticipationRepository.findById(anyInt())).thenReturn(testParticipation);
        when(eventRepository.findById(anyInt())).thenReturn(testEvent);
        when(userService.getUserDTOById(anyInt())).thenReturn(null);

        // 执行测试
        EventParticipationDTO result = eventService.getParticipationById(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(testParticipation.getUserId(), result.getUserId());
    }
} 