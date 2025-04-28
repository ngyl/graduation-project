package com.animesocial.platform.service;

import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.model.dto.EventParticipationDTO;

import java.time.LocalDate;
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
    List<EventDTO> getAllEvents(Integer status, LocalDate startTime, LocalDate endTime);
    
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
     * @return 活动列表
     */
    List<EventDTO> getUpcomingEvents();
    
    /**
     * 获取即将开始的活动(指定天数)
     * @param days 未来天数
     * @return 活动列表
     */
    List<EventDTO> getUpcomingEvents(Integer days);

    /**
     * 获取用户参加的活动列表
     * @param userId 用户ID
     * @return 活动列表
     */
    List<EventDTO> getUserEvents(Integer userId);
    
    /**
     * 用户参加活动
     * @param userId 用户ID
     * @param eventId 活动ID
     */
    void participateEvent(Integer userId, Integer eventId);
    
    /**
     * 用户取消参加活动
     * @param userId 用户ID
     * @param eventId 活动ID
     */
    void cancelParticipation(Integer userId, Integer eventId);
    
    /**
     * 检查用户是否参加了活动
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否已参加
     */
    boolean checkParticipation(Integer userId, Integer eventId);
    
    /**
     * 检查活动是否已结束
     * @param id 活动ID
     * @return true表示已结束，false表示未结束
     */
    boolean isEventEnded(Integer id);
    
    /**
     * 获取活动总数
     * @param status 活动状态(可选，0下线，1上线，null表示所有)
     * @return 活动总数
     */
    int countEvents(Integer status);
    
    /**
     * 获取活动的参与者列表
     * @param eventId 活动ID
     * @return 参与者列表
     */
    List<EventParticipationDTO> getEventParticipants(Integer eventId);
    
    /**
     * 获取用户参与的活动记录
     * @param userId 用户ID
     * @return 用户参与的活动记录列表
     */
    List<EventParticipationDTO> getUserParticipations(Integer userId);
    
    /**
     * 根据ID获取活动参与记录
     * @param id 记录ID
     * @return 活动参与记录详情
     */
    EventParticipationDTO getParticipationById(Integer id);
} 