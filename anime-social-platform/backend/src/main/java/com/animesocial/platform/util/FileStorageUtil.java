package com.animesocial.platform.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件存储工具类
 * 处理文件上传、删除等操作
 */
@Component
@Slf4j
public class FileStorageUtil {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 存储文件
     * @param file 待存储的文件
     * @return 存储后的文件名
     * @throws IOException 如果存储过程中发生IO错误
     */
    public String storeFile(MultipartFile file) throws IOException {
        // 检查上传目录是否存在，不存在则创建
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
        
        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 生成唯一的文件名
        String fileName = UUID.randomUUID().toString() + extension;
        
        // 构建文件路径
        Path targetLocation = Paths.get(uploadDir, fileName);
        
        // 保存文件
        Files.copy(file.getInputStream(), targetLocation);
        
        log.info("文件 {} 保存至 {}", originalFilename, targetLocation);
        
        return fileName;
    }
    
    /**
     * 删除文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileName) {
        Path filePath = Paths.get(uploadDir, fileName);
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 获取文件路径
     * @param fileName 文件名
     * @return 完整的文件路径
     */
    public String getFilePath(String fileName) {
        return Paths.get(uploadDir, fileName).toString();
    }
} 