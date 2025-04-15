package com.animesocial.platform.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.animesocial.platform.exception.BusinessException;
import com.animesocial.platform.model.User;
import com.animesocial.platform.model.dto.LoginRequest;
import com.animesocial.platform.model.dto.RegisterRequest;
import com.animesocial.platform.model.dto.UserDTO;
import com.animesocial.platform.repository.UserRepository;
import com.animesocial.platform.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        // 验证密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已存在
        if (isUsernameExist(registerRequest.getUsername())) {
            throw new BusinessException("用户名已被使用");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setIsAdmin(false);
        user.setStatus(1); // 正常状态
        user.setRegisterTime(LocalDateTime.now());
        
        // 保存用户
        userRepository.insert(user);
    }

    @Override
    public User login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证用户是否被禁用
        if (user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.updateLastLoginTime(user);
        
        // 将用户信息存入session
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
        }
        
        return user;
    }

    @Override
    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id);
        if (user == null) {
            return null;
        }
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean isUsernameExist(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Override
    public UserDTO getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return null;
        }
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
} 