package com.animesocial.platform.controller;

import com.animesocial.platform.model.dto.ApiResponse;
import com.animesocial.platform.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 处理所有文件上传请求，包括头像、各类资源文件等
 */
@RestController
@RequestMapping("/api")
public class FileController {
    
    @Autowired
    private FileStorageUtil fileStorageUtil;
    
    @Value("${app.file.domain}")
    private String fileDomain;
    
    /**
     * 通用文件上传接口
     * 根据文件类型分别存储到不同目录
     * 
     * @param file 上传的文件
     * @param type 文件类型（avatar: 头像，resource: 资源文件等）
     * @param session HTTP会话
     * @return 上传结果，包含文件URL
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.validateFailed("请选择文件上传");
        }
        
        try {
            String storedFilePath;
            String fileType = fileStorageUtil.getFileType(file.getOriginalFilename());
            
            // 根据type参数决定存储位置
            if ("avatar".equals(type)) {
                storedFilePath = fileStorageUtil.storeAvatarFile(file);
            } else if ("resource".equals(type)) {
                storedFilePath = fileStorageUtil.storeResourceFile(file, fileType);
            } else {
                // 通用文件存储
                storedFilePath = fileStorageUtil.storeFile(file, "common");
            }
            
            // 构建文件访问URL
            String fileUrl = fileDomain + "/files/" + storedFilePath;
            
            // 返回文件URL和相关信息
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filePath", storedFilePath);
            result.put("fileType", fileType);
            result.put("originalFilename", file.getOriginalFilename());
            
            return ApiResponse.success("文件上传成功", result);
        } catch (IOException e) {
            return ApiResponse.failed("文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 头像上传专用接口
     * 
     * @param file 上传的头像文件
     * @param session HTTP会话
     * @return 上传结果，包含头像URL
     */
    @PostMapping("/upload/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.validateFailed("请选择头像图片上传");
        }
        
        // 验证是否为图片
        String fileType = fileStorageUtil.getFileType(file.getOriginalFilename());
        if (!"image".equals(fileType)) {
            return ApiResponse.validateFailed("头像必须是图片格式（JPG/PNG/GIF等）");
        }
        
        try {
            // 存储头像文件
            String storedFilePath = fileStorageUtil.storeAvatarFile(file);
            
            // 构建头像访问URL
            String fileUrl = fileDomain + "/files/" + storedFilePath;
            
            // 返回头像URL
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filePath", storedFilePath);
            
            return ApiResponse.success("头像上传成功", result);
        } catch (IOException e) {
            return ApiResponse.failed("头像上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 资源文件上传专用接口
     * 
     * @param file 上传的资源文件
     * @param session HTTP会话
     * @return 上传结果，包含资源文件URL和类型
     */
    @PostMapping("/upload/resource")
    public ApiResponse<Map<String, String>> uploadResourceFile(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.validateFailed("请选择资源文件上传");
        }
        
        try {
            // 获取文件类型
            String fileType = fileStorageUtil.getFileType(file.getOriginalFilename());
            
            // 存储资源文件
            String storedFilePath = fileStorageUtil.storeResourceFile(file, fileType);
            
            // 构建资源文件访问URL
            String fileUrl = fileDomain + "/files/" + storedFilePath;
            
            // 返回资源文件URL和相关信息
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filePath", storedFilePath);
            result.put("fileType", fileType);
            result.put("originalFilename", file.getOriginalFilename());
            
            return ApiResponse.success("资源文件上传成功", result);
        } catch (IOException e) {
            return ApiResponse.failed("资源文件上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 资源封面图上传专用接口
     * 
     * @param file 上传的封面图文件
     * @param session HTTP会话
     * @return 上传结果，包含封面图URL
     */
    @PostMapping("/upload/cover")
    public ApiResponse<Map<String, String>> uploadCoverImage(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        
        // 检查用户是否登录
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized();
        }
        
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.validateFailed("请选择封面图片上传");
        }
        
        // 验证是否为图片
        String fileType = fileStorageUtil.getFileType(file.getOriginalFilename());
        if (!"image".equals(fileType)) {
            return ApiResponse.validateFailed("封面必须是图片格式（JPG/PNG/GIF等）");
        }
        
        try {
            // 存储封面图片
            String storedFilePath = fileStorageUtil.storeFile(file, "covers");
            
            // 构建封面图片访问URL
            String fileUrl = fileDomain + "/files/" + storedFilePath;
            
            // 返回封面图片URL
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filePath", storedFilePath);
            
            return ApiResponse.success("封面图片上传成功", result);
        } catch (IOException e) {
            return ApiResponse.failed("封面图片上传失败：" + e.getMessage());
        }
    }
} 