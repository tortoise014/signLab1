package com.signlab1.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
public class FileUploadUtil {
    
    @Value("${file.upload.photo.path}")
    private String photoUploadPath;
    
    /**
     * 上传课堂照片
     * @param file 上传的文件
     * @param courseId 课程ID
     * @param studentUsername 学生用户名
     * @param remark 照片备注
     * @return 文件信息
     */
    public FileUploadResult uploadClassPhoto(MultipartFile file, String courseId, String studentUsername, String remark) {
        try {
            // 验证文件
            validatePhotoFile(file);
            
            // 生成文件路径
            String filePath = generatePhotoPath(courseId, studentUsername, file.getOriginalFilename());
            
            // 创建目录
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            // 保存文件
            file.transferTo(path.toFile());
            
            // 返回文件信息
            FileUploadResult result = new FileUploadResult();
            result.setFileName(path.getFileName().toString());
            result.setFilePath(filePath);
            result.setFileSize(file.getSize());
            result.setUploadTime(LocalDateTime.now());
            result.setRemark(remark);
            
            log.info("照片上传成功: 学生={}, 课程={}, 文件={}", studentUsername, courseId, result.getFileName());
            return result;
            
        } catch (IOException e) {
            log.error("照片上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("照片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证照片文件
     */
    private void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的照片");
        }
        
        // 检查文件大小 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("照片大小不能超过10MB");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("不支持的图片格式，请上传 jpg、jpeg、png、gif 格式的图片");
        }
    }
    
    /**
     * 生成照片存储路径
     * 格式: uploads/signlab/photos/年/月/日/课程ID/学生学号_时间戳.扩展名
     */
    private String generatePhotoPath(String courseId, String studentUsername, String originalFilename) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        
        // 生成唯一文件名: 学生学号_日期_时间戳_随机码.扩展名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        String fileName = String.format("%s_%s_%s.%s", studentUsername, timestamp, randomCode, extension);
        
        // 构建完整路径
        return String.format("%s/%s/%s/%s/%s/%s", 
            photoUploadPath, year, month, day, courseId, fileName);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    /**
     * 检查是否为有效的图片扩展名
     */
    private boolean isValidImageExtension(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) || 
               "png".equals(extension) || "gif".equals(extension);
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: {}", filePath);
                return true;
            } else {
                log.warn("文件不存在: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 文件上传结果
     */
    public static class FileUploadResult {
        private String fileName;
        private String filePath;
        private Long fileSize;
        private LocalDateTime uploadTime;
        private String remark;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public LocalDateTime getUploadTime() { return uploadTime; }
        public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
        
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}

