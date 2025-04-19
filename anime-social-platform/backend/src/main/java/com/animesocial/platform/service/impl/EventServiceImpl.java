package com.animesocial.platform.service.impl;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.Event;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.repository.EventRepository;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.EventService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
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
    public List<EventDTO> getAllEvents(Integer status, LocalDateTime startTime, LocalDateTime endTime) {
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
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!user.getIsAdmin()) {
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
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findCurrentEvents(now);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取即将开始的活动
     */
    @Override
    public List<EventDTO> getUpcomingEvents(Integer days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);
        List<Event> events = eventRepository.findUpcomingEvents(now, future);
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(event.getEndTime());
    }
    
    /**
     * 将Event实体转换为EventDTO
     */
    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        BeanUtils.copyProperties(event, dto);
        
        // 加载创建者信息
        User creator = userRepository.findById(event.getCreatedBy());
        if (creator != null) {
            dto.setCreatorName(creator.getUsername());
        }
        
        return dto;
    }
} 