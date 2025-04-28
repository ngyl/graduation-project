package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Event;
import com.animesocial.platform.model.EventParticipation;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.model.dto.EventParticipationDTO;
import com.animesocial.platform.repository.EventParticipationRepository;
import com.animesocial.platform.repository.EventRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.EventService;
import com.animesocial.platform.service.UserService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Service
@Slf4j
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventParticipationRepository eventParticipationRepository;
    
    @Autowired
    private UserService userService;

    /**
     * 根据ID获取活动
     */
    @Override
    public EventDTO getEventById(Integer id) {
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        return convertToDTO(event);
    }

    /**
     * 获取所有活动
     */
    @Override
    public List<EventDTO> getAllEvents(Integer status, LocalDate startTime, LocalDate endTime) {
        List<Event> events = eventRepository.findAllByFilter(status, startTime, endTime);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 创建活动
     */
    @Override
    @Transactional
    public EventDTO createEvent(Integer userId, CreateEventRequest request) {
        // 验证用户是否存在且是管理员
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        if (!userRepository.isAdmin(userId)) {
            throw new BusinessException("只有管理员可以创建活动");
        }
        
        // 验证时间
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        
        // 创建活动
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setStatus(1); // 默认上线
        event.setCreatedBy(userId);
        
        // 保存活动
        eventRepository.insert(event);
        
        return convertToDTO(event);
    }

    /**
     * 更新活动
     */
    @Override
    @Transactional
    public EventDTO updateEvent(Integer id, CreateEventRequest request) {
        // 验证活动是否存在
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 验证时间
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        
        // 更新活动信息
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        
        // 保存更新
        eventRepository.update(event);
        
        return convertToDTO(event);
    }

    /**
     * 更新活动状态
     */
    @Override
    @Transactional
    public void updateEventStatus(Integer id, Integer status) {
        // 验证活动是否存在
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 验证状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值必须是0(下线)或1(上线)");
        }
        
        // 更新状态
        eventRepository.updateStatus(id, status);
    }

    /**
     * 删除活动
     */
    @Override
    @Transactional
    public void deleteEvent(Integer id) {
        // 验证活动是否存在
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 删除活动
        eventRepository.delete(id);
    }

    /**
     * 获取当前进行中的活动
     */
    @Override
    public List<EventDTO> getCurrentEvents() {
        LocalDate now = LocalDate.now();
        List<Event> events = eventRepository.findCurrentEvents(now);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取即将开始的活动（默认7天内）
     */
    @Override
    public List<EventDTO> getUpcomingEvents() {
        return getUpcomingEvents(7); // 默认查询未来7天的活动
    }

    /**
     * 获取即将开始的活动（指定天数）
     */
    @Override
    public List<EventDTO> getUpcomingEvents(Integer days) {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(days);
        List<Event> events = eventRepository.findUpcomingEvents(now, future);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户参加的活动列表
     */
    @Override
    public List<EventDTO> getUserEvents(Integer userId) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取用户参加的所有活动ID
        List<Integer> eventIds = eventParticipationRepository.findEventIdsByUserId(userId);
        
        // 查询这些活动的详细信息
        List<Event> events = eventRepository.findAllByIds(eventIds);
        
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 用户参加活动
     */
    @Override
    @Transactional
    public void participateEvent(Integer userId, Integer eventId) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证活动是否存在
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 验证活动是否已结束
        if (isEventEnded(eventId)) {
            throw new BusinessException("活动已结束，无法报名");
        }
        
        // 验证活动是否已经上线
        if (event.getStatus() != 1) {
            throw new BusinessException("活动尚未上线，无法报名");
        }
        
        // 检查是否已报名
        if (checkParticipation(userId, eventId)) {
            throw new BusinessException("您已报名该活动，请勿重复报名");
        }
        
        // 创建报名记录
        EventParticipation participation = new EventParticipation();
        participation.setUserId(userId);
        participation.setEventId(eventId);
        participation.setParticipationTime(LocalDateTime.now());
        participation.setStatus(1); // 已报名状态
        
        // 保存报名记录
        eventParticipationRepository.insert(participation);
    }

    /**
     * 用户取消参加活动
     */
    @Override
    @Transactional
    public void cancelParticipation(Integer userId, Integer eventId) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证活动是否存在
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 检查是否已报名
        if (!checkParticipation(userId, eventId)) {
            throw new BusinessException("您未报名该活动");
        }
        
        // 删除报名记录
        eventParticipationRepository.delete(userId, eventId);
    }

    /**
     * 检查用户是否参加了活动
     */
    @Override
    public boolean checkParticipation(Integer userId, Integer eventId) {
        return eventParticipationRepository.exists(userId, eventId);
    }

    /**
     * 检查活动是否已结束
     */
    @Override
    public boolean isEventEnded(Integer id) {
        Event event = eventRepository.findById(id);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        LocalDate now = LocalDate.now();
        return now.isAfter(event.getEndTime());
    }
    
    /**
     * 将Event实体转换为EventDTO
     */
    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        BeanUtils.copyProperties(event, dto);
        
        // 加载创建者信息
        if (!userRepository.existsByIdOrUsername(event.getCreatedBy(), null)) {
            dto.setCreatorName(userRepository.findUsernameById(event.getCreatedBy()));
        }
        
        // 设置isOngoing标志
        LocalDate now = LocalDate.now();
        boolean isOngoing = !now.isBefore(event.getStartTime()) && !now.isAfter(event.getEndTime());
        dto.setIsOngoing(isOngoing);
        
        return dto;
    }

    /**
     * 获取活动总数
     * @param status 活动状态(可选，0下线，1上线，null表示所有)
     * @return 活动总数
     */
    @Override
    public int countEvents(Integer status) {
        return eventRepository.countByStatus(status);
    }
    
    /**
     * 获取活动的参与者列表
     * @param eventId 活动ID
     * @return 参与者列表
     */
    @Override
    public List<EventParticipationDTO> getEventParticipants(Integer eventId) {
        // 验证活动是否存在
        Event event = eventRepository.findById(eventId);
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 获取参与者列表
        List<Integer> userIds = eventParticipationRepository.findUserIdsByEventId(eventId);
        List<EventParticipation> participations = new ArrayList<>();
        
        for (Integer userId : userIds) {
            EventParticipation participation = new EventParticipation();
            participation.setUserId(userId);
            participation.setEventId(eventId);
            
            // 这里可以通过查询获取更多参与信息，如参与时间等
            // 但示例中简化处理
            
            participations.add(participation);
        }
        
        // 转换为DTO
        return participations.stream()
                .map(p -> convertToParticipationDTO(p, event))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户参与的活动记录
     * @param userId 用户ID
     * @return 用户参与的活动记录列表
     */
    @Override
    public List<EventParticipationDTO> getUserParticipations(Integer userId) {
        // 验证用户是否存在
        if (!userRepository.existsByIdOrUsername(userId, null)) {
            throw new BusinessException("用户不存在");
        }
        
        // 获取用户参加的活动ID列表
        List<Integer> eventIds = eventParticipationRepository.findEventIdsByUserId(userId);
        List<EventParticipationDTO> dtoList = new ArrayList<>();
        
        for (Integer eventId : eventIds) {
            Event event = eventRepository.findById(eventId);
            if (event != null) {
                EventParticipation participation = new EventParticipation();
                participation.setUserId(userId);
                participation.setEventId(eventId);
                
                // 这里可以通过查询获取更多参与信息，如参与时间等
                // 但示例中简化处理
                
                dtoList.add(convertToParticipationDTO(participation, event));
            }
        }
        
        return dtoList;
    }
    
    /**
     * 根据ID获取活动参与记录
     * @param id 记录ID
     * @return 活动参与记录详情
     */
    @Override
    public EventParticipationDTO getParticipationById(Integer id) {
        // 查询活动参与记录
        EventParticipation participation = eventParticipationRepository.findById(id);
        if (participation == null) {
            throw new BusinessException("活动参与记录不存在");
        }
        
        // 查询活动信息
        Event event = eventRepository.findById(participation.getEventId());
        if (event == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 转换为DTO并返回
        return convertToParticipationDTO(participation, event);
    }
    
    /**
     * 将EventParticipation实体转换为EventParticipationDTO
     * @param participation 活动参与实体
     * @param event 活动实体，可选，如果为null会重新查询
     * @return 活动参与DTO
     */
    private EventParticipationDTO convertToParticipationDTO(EventParticipation participation, Event event) {
        EventParticipationDTO dto = new EventParticipationDTO();
        BeanUtils.copyProperties(participation, dto);
        
        // 获取用户信息
        UserDTO user = userService.getUserDTOById(participation.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        
        // 获取活动信息
        if (event == null) {
            event = eventRepository.findById(participation.getEventId());
        }
        
        if (event != null) {
            dto.setEventTitle(event.getTitle());
            dto.setEventStartTime(event.getStartTime());
            dto.setEventEndTime(event.getEndTime());
            
            // 设置是否进行中
            LocalDate now = LocalDate.now();
            boolean isOngoing = !now.isBefore(event.getStartTime()) && !now.isAfter(event.getEndTime());
            dto.setIsOngoing(isOngoing);
        }
        
        return dto;
    }
} 