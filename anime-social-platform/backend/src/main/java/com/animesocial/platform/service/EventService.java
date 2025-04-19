package com.animesocial.platform.service;

import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.CreateEventRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动服务接口
 * 定义活动相关的业务逻辑方法
 */
public interface EventService {
    
    /**
     * 根据ID获取活动
     * @param id 活动ID
     * @return 活动详情
     */
    EventDTO getEventById(Integer id);
    
    /**
     * 获取所有活动
     * @param status 活动状态(可选，0下线，1上线)
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @return 活动列表
     */
    List<EventDTO> getAllEvents(Integer status, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 创建活动
     * @param userId 创建者ID
     * @param request 创建活动请求
     * @return 创建的活动
     */
    EventDTO createEvent(Integer userId, CreateEventRequest request);
    
    /**
     * 更新活动
     * @param id 活动ID
     * @param request 更新活动请求
     * @return 更新后的活动
     */
    EventDTO updateEvent(Integer id, CreateEventRequest request);
    
    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 活动状态(0下线，1上线)
     */
    void updateEventStatus(Integer id, Integer status);
    
    /**
     * 删除活动
     * @param id 活动ID
     */
    void deleteEvent(Integer id);
    
    /**
     * 获取当前进行中的活动
     * @return 活动列表
     */
    List<EventDTO> getCurrentEvents();
    
    /**
     * 获取即将开始的活动
     * @param days 未来天数
     * @return 活动列表
     */
    List<EventDTO> getUpcomingEvents(Integer days);
    
    /**
     * 检查活动是否已结束
     * @param id 活动ID
     * @return true表示已结束，false表示未结束
     */
    boolean isEventEnded(Integer id);
} 