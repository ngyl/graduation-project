package com.animesocial.platform.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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
    
    @Value("${app.file.domain}")
    private String fileDomain;
    
    // 需要创建的子目录列表
    private final List<String> REQUIRED_SUBDIRS = Arrays.asList(
        "avatars", 
        "resources/image", 
        "resources/video",
        "resources/audio",
        "resources/document",
        "resources/archive",
        "covers",
        "common"
    );
    
    /**
     * 初始化时创建所有必需的目录
     */
    @PostConstruct
    public void init() {
        try {
            // 创建主上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传主目录: {}", uploadPath);
            }
            
            // 创建所有必需的子目录
            for (String subDir : REQUIRED_SUBDIRS) {
                Path subDirPath = Paths.get(uploadDir, subDir);
                if (!Files.exists(subDirPath)) {
                    Files.createDirectories(subDirPath);
                    log.info("创建上传子目录: {}", subDirPath);
                }
            }
        } catch (IOException e) {
            log.error("无法创建上传目录: {}", e.getMessage(), e);
            throw new RuntimeException("无法创建文件上传目录", e);
        }
    }

    /**
     * 存储文件到默认目录
     * @param file 待存储的文件
     * @return 存储后的文件名
     * @throws IOException 如果存储过程中发生IO错误
     */
    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "");
    }

    /**
     * 按照类型存储文件到指定子目录
     * @param file 待存储的文件
     * @param subDir 子目录，如avatar, resource等
     * @return 存储后的文件名（包含子目录路径）
     * @throws IOException 如果存储过程中发生IO错误
     */
    public String storeFile(MultipartFile file, String subDir) throws IOException {
        // 构建目标目录路径
        String targetDirPath = uploadDir;
        if (subDir != null && !subDir.isEmpty()) {
            targetDirPath = Paths.get(uploadDir, subDir).toString();
        }
        
        // 检查上传目录是否存在，不存在则创建
        File uploadPath = new File(targetDirPath);
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
        
        // 构建文件保存路径
        Path targetLocation = Paths.get(targetDirPath, fileName);
        
        // 保存文件
        Files.copy(file.getInputStream(), targetLocation);
        
        log.info("文件 {} 保存至 {}", originalFilename, targetLocation);
        
        // 返回包含子目录的相对路径
        return subDir.isEmpty() ? fileName : Paths.get(subDir, fileName).toString();
    }
    
    /**
     * 按照文件类型存储资源文件
     * @param file 待存储的文件
     * @param fileType 文件类型，如image, video, document等
     * @return 存储后的文件名（包含分类目录路径）
     * @throws IOException 如果存储过程中发生IO错误
     */
    public String storeResourceFile(MultipartFile file, String fileType) throws IOException {
        // 资源文件默认存储在resources目录下，并按照类型进一步分类
        String subDir = Paths.get("resources", fileType).toString();
        return storeFile(file, subDir);
    }
    
    /**
     * 存储头像文件
     * @param file 待存储的文件
     * @return 存储后的文件名（包含头像目录路径）
     * @throws IOException 如果存储过程中发生IO错误
     */
    public String storeAvatarFile(MultipartFile file) throws IOException {
        return storeFile(file, "avatars");
    }
    
    /**
     * 删除文件
     * @param filePath 文件相对路径，如avatars/xxx.jpg
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        // 如果路径包含域名或http前缀，需要去除
        if (filePath.startsWith(fileDomain)) {
            filePath = filePath.substring(fileDomain.length());
        }
        
        // 去除开头的斜杠（如果有）
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        
        Path fullPath = Paths.get(uploadDir, filePath);
        try {
            boolean result = Files.deleteIfExists(fullPath);
            if (result) {
                log.info("成功删除文件: {}", fullPath);
            } else {
                log.warn("文件不存在，无法删除: {}", fullPath);
            }
            return result;
        } catch (IOException e) {
            log.error("删除文件失败: {}", fullPath, e);
            return false;
        }
    }
    
    /**
     * 获取文件路径
     * @param filePath 文件相对路径
     * @return 完整的文件路径
     */
    public String getFilePath(String filePath) {
        return Paths.get(uploadDir, filePath).toString();
    }
    
    /**
     * 获取文件的公共访问URL
     * @param relativePath 文件的相对路径
     * @return 完整的访问URL
     */
    public String getPublicUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        
        // 如果路径已经包含域名，直接返回
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        
        // 确保路径有正确的格式
        String path = relativePath;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        // 避免重复路径片段
        if (path.contains("/files/") && !fileDomain.endsWith("/files")) {
            path = path.replace("/files/", "/");
        }
        
        // 记录生成的URL，方便调试
        String url = fileDomain + path;
        log.debug("生成公共URL: {} -> {}", relativePath, url);
        
        return url;
    }
    
    /**
     * 获取文件类型
     * @param filename 原始文件名
     * @return 文件类型，如image, video, document等
     */
    public String getFileType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown";
        }
        
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "unknown";
        }
        
        String extension = filename.substring(dotIndex + 1).toLowerCase();
        
        // 简单的文件类型判断
        if (extension.matches("jpg|jpeg|png|gif|webp|bmp")) {
            return "image";
        } else if (extension.matches("mp4|avi|mov|wmv|flv|mkv")) {
            return "video";
        } else if (extension.matches("mp3|ogg|wav|flac|aac")) {
            return "audio";
        } else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt")) {
            return "document";
        } else if (extension.matches("zip|rar|7z|tar|gz")) {
            return "archive";
        }
        
        return extension;
    }
}