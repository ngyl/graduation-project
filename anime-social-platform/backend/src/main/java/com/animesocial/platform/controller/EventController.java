package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.model.dto.EventDTO;
import com.animesocial.platform.model.dto.CreateEventRequest;
import com.animesocial.platform.model.dto.EventParticipationDTO;
import com.animesocial.platform.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 活动控制器
 * 处理与活动相关的所有HTTP请求，包括：
 * 1. 活动发布和管理
 * 2. 活动查询和筛选
 * 3. 活动参与管理
 */
@RestController
@RequestMapping("/api/event")
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime) {
        try {
            return ApiResponse.success(eventService.getAllEvents(status, startTime, endTime));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取当前进行中的活动
     * @return 进行中的活动列表
     */
    @GetMapping("/current")
    public ApiResponse<List<EventDTO>> getCurrentEvents() {
        try {
            return ApiResponse.success(eventService.getCurrentEvents());
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取即将开始的活动
     * @param days 未来几天内的活动(可选)
     * @return 即将开始的活动列表
     */
    @GetMapping("/upcoming")
    public ApiResponse<List<EventDTO>> getUpcomingEvents(
            @RequestParam(required = false) Integer days) {
        try {
            if (days != null) {
                return ApiResponse.success(eventService.getUpcomingEvents(days));
            } else {
                return ApiResponse.success(eventService.getUpcomingEvents());
            }
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 获取用户参加的活动列表
     * @param session HTTP会话
     * @return 用户参加的活动列表
     */
    @GetMapping("/user-events")
    public ApiResponse<List<EventDTO>> getUserEvents(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(eventService.getUserEvents(userId));
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
     * 创建新活动(前端用户创建的端点)
     * @param request 创建活动请求对象
     * @param session HTTP会话
     * @return 新创建的活动对象
     */
    @PostMapping("/create")
    public ApiResponse<EventDTO> createUserEvent(@Valid @RequestBody CreateEventRequest request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            // 创建状态为待审核(0)的活动
            EventDTO event = eventService.createEvent(userId, request);
            return ApiResponse.success("活动创建成功，等待审核", event);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 参加活动
     * @param requestBody 请求体，包含eventId
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/participate")
    public ApiResponse<Void> participateEvent(@RequestBody Map<String, Integer> requestBody, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        Integer eventId = requestBody.get("eventId");
        if (eventId == null) {
            return ApiResponse.validateFailed("缺少活动ID参数");
        }
        
        try {
            eventService.participateEvent(userId, eventId);
            return ApiResponse.success("活动报名成功", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 取消参加活动
     * @param requestBody 请求体，包含eventId
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/cancel-participation")
    public ApiResponse<Void> cancelParticipation(@RequestBody Map<String, Integer> requestBody, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        Integer eventId = requestBody.get("eventId");
        if (eventId == null) {
            return ApiResponse.validateFailed("缺少活动ID参数");
        }
        
        try {
            eventService.cancelParticipation(userId, eventId);
            return ApiResponse.success("已取消报名", null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    /**
     * 检查用户是否已参加活动
     * @param eventId 活动ID
     * @param session HTTP会话
     * @return 是否已参加
     */
    @GetMapping("/check-participation")
    public ApiResponse<Boolean> checkParticipation(@RequestParam Integer eventId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            boolean participated = eventService.checkParticipation(userId, eventId);
            return ApiResponse.success(participated);
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

    /**
     * 获取活动参与者列表
     * @param eventId 活动ID
     * @return 参与者列表
     */
    @GetMapping("/{id}/participants")
    public ApiResponse<List<EventParticipationDTO>> getEventParticipants(@PathVariable("id") Integer eventId) {
        try {
            return ApiResponse.success(eventService.getEventParticipants(eventId));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
    
    /**
     * 获取用户参与的活动记录
     * @param session HTTP会话
     * @return 用户参与的活动记录列表
     */
    @GetMapping("/participations")
    public ApiResponse<List<EventParticipationDTO>> getUserParticipations(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        try {
            return ApiResponse.success(eventService.getUserParticipations(userId));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
} 