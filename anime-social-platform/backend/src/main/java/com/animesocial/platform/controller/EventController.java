package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动控制器
 * 处理与活动相关的所有HTTP请求，包括：
 * 1. 活动发布和管理
 * 2. 活动查询和筛选
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    /**
     * 获取指定ID的活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    @GetMapping("/{id}")
    public ApiResponse<EventDTO> getEvent(@PathVariable Integer id) {
        try {
            return ApiResponse.success(eventService.getEventById(id));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取所有活动
     * @param status 活动状态(可选，0下线，1上线)
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @return 活动列表
     */
    @GetMapping
    public ApiResponse<List<EventDTO>> getAllEvents(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            return ApiResponse.success(eventService.getAllEvents(status, startTime, endTime));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 创建新活动(仅管理员)
     * @param request 创建活动请求对象
     * @param session HTTP会话
     * @return 新创建的活动对象
     */
    @PostMapping
    public ApiResponse<EventDTO> createEvent(@Valid @RequestBody CreateEventRequest request, HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            EventDTO event = eventService.createEvent(userId, request);
            return ApiResponse.success("活动创建成功", event);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 更新活动(仅管理员)
     * @param id 活动ID
     * @param request 更新活动请求对象
     * @param session HTTP会话
     * @return 更新后的活动对象
     */
    @PutMapping("/{id}")
    public ApiResponse<EventDTO> updateEvent(
            @PathVariable Integer id,
            @Valid @RequestBody CreateEventRequest request,
            HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        try {
            EventDTO event = eventService.updateEvent(id, request);
            return ApiResponse.success("活动更新成功", event);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 修改活动状态(仅管理员)
     * @param id 活动ID
     * @param status 活动状态(0下线，1上线)
     * @param session HTTP会话
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateEventStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        if (status != 0 && status != 1) {
            return ApiResponse.validateFailed("状态值必须是0(下线)或1(上线)");
        }
        
        try {
            eventService.updateEventStatus(id, status);
            return ApiResponse.success(status == 1 ? "活动已上线" : "活动已下线", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 删除活动(仅管理员)
     * @param id 活动ID
     * @param session HTTP会话
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Integer id, HttpSession session) {
        // 检查管理员权限
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ApiResponse.forbidden();
        }
        
        try {
            eventService.deleteEvent(id);
            return ApiResponse.success("活动已删除", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 