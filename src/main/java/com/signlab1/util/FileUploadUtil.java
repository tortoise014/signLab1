package com.signlab1.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
public class FileUploadUtil {
    
    @Value("${file.upload.photo.path}")
    private String photoUploadPath;
    
    @Autowired
    private ImageCompressionUtil imageCompressionUtil;
    
    /**
     * 上传课堂照片（双版本存储：原图+压缩图）
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
            
            // 生成原图文件路径
            String originalFilePath = generatePhotoPath(courseId, studentUsername, file.getOriginalFilename(), "original");
            
            // 创建原图目录
            Path originalPath = Paths.get(originalFilePath);
            Files.createDirectories(originalPath.getParent());
            
            // 保存原图
            file.transferTo(originalPath.toFile());
            
            // 生成压缩图
            String compressedFilePath = generateCompressedPhoto(originalFilePath, courseId, studentUsername, file.getOriginalFilename());
            
            // 返回文件信息
            FileUploadResult result = new FileUploadResult();
            result.setFileName(originalPath.getFileName().toString());
            result.setFilePath(originalFilePath);
            result.setCompressedFilePath(compressedFilePath);
            result.setFileSize(file.getSize());
            result.setUploadTime(LocalDateTime.now());
            result.setRemark(remark);
            
            log.info("照片上传成功: 学生={}, 课程={}, 原图={}, 压缩图={}", 
                    studentUsername, courseId, result.getFileName(), compressedFilePath);
            return result;
            
        } catch (Exception e) {
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
     * 格式: uploads/signlab/photos/年/月/日/课程ID/学生学号/类型/时间戳.扩展名
     */
    private String generatePhotoPath(String courseId, String studentUsername, String originalFilename, String type) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day = String.format("%02d", now.getDayOfMonth());
        
        // 生成唯一文件名: 时间戳_随机码.扩展名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        String fileName = String.format("%s_%s.%s", timestamp, randomCode, extension);
        
        // 构建完整路径：日期 -> 课程 -> 学号 -> 类型
        return String.format("%s/%s/%s/%s/%s/%s/%s/%s", 
            photoUploadPath, year, month, day, courseId, studentUsername, type, fileName);
    }
    
    /**
     * 生成压缩照片
     */
    private String generateCompressedPhoto(String originalFilePath, String courseId, String studentUsername, String originalFilename) {
        try {
            // 生成压缩图路径
            String compressedFilePath = generatePhotoPath(courseId, studentUsername, originalFilename, "compressed");
            
            // 读取原图
            File originalFile = new File(originalFilePath);
            if (!originalFile.exists()) {
                throw new RuntimeException("原图文件不存在: " + originalFilePath);
            }
            
            // 压缩图像
            BufferedImage originalImage = ImageIO.read(originalFile);
            BufferedImage compressedImage = imageCompressionUtil.compressImage(originalImage);
            
            // 保存压缩图
            String format = imageCompressionUtil.getImageFormat(originalFilename);
            imageCompressionUtil.saveCompressedImage(compressedImage, compressedFilePath, format);
            
            // 释放内存
            originalImage.flush();
            compressedImage.flush();
            
            log.info("压缩图生成成功: {}", compressedFilePath);
            return compressedFilePath;
            
        } catch (Exception e) {
            log.error("生成压缩图失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成压缩图失败: " + e.getMessage());
        }
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
        private String compressedFilePath;
        private Long fileSize;
        private LocalDateTime uploadTime;
        private String remark;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getCompressedFilePath() { return compressedFilePath; }
        public void setCompressedFilePath(String compressedFilePath) { this.compressedFilePath = compressedFilePath; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public LocalDateTime getUploadTime() { return uploadTime; }
        public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
        
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}

