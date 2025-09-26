package com.signlab1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 目录初始化器 - 在应用启动时自动创建必要的目录
 */
@Slf4j
@Component
public class DirectoryInitializer implements CommandLineRunner {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.photo.path}")
    private String photoPath;

    @Value("${file.upload.document.path}")
    private String documentPath;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化上传目录...");
        
        // 创建基础目录
        createDirectoryIfNotExists(uploadPath, "上传根目录");
        createDirectoryIfNotExists(photoPath, "照片存储目录");
        createDirectoryIfNotExists(documentPath, "文档存储目录");
        
        // 创建照片子目录结构示例（按日期/课程/学号/类型）
        createPhotoSubDirectories();
        
        log.info("目录初始化完成！");
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String path, String description) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("创建目录成功: {} ({})", path, description);
            } else {
                log.info("目录已存在: {} ({})", path, description);
            }
        } catch (Exception e) {
            log.error("创建目录失败: {} ({}) - {}", path, description, e.getMessage(), e);
        }
    }

    /**
     * 创建照片子目录结构
     * 格式: photos/年/月/日/课程ID/学号/类型
     */
    private void createPhotoSubDirectories() {
        try {
            // 获取当前日期
            java.time.LocalDate now = java.time.LocalDate.now();
            String year = String.valueOf(now.getYear());
            String month = String.format("%02d", now.getMonthValue());
            String day = String.format("%02d", now.getDayOfMonth());
            
            // 创建当前日期的目录结构示例
            String currentDatePath = String.format("%s/%s/%s", photoPath, year, month, day);
            createDirectoryIfNotExists(currentDatePath, "当前日期目录");
            
            // 创建示例课程目录
            String exampleCoursePath = String.format("%s/example_course", currentDatePath);
            createDirectoryIfNotExists(exampleCoursePath, "示例课程目录");
            
            // 创建示例学生目录
            String exampleStudentPath = String.format("%s/example_student", exampleCoursePath);
            createDirectoryIfNotExists(exampleStudentPath, "示例学生目录");
            
            // 创建原图和压缩图目录
            String originalPath = String.format("%s/original", exampleStudentPath);
            String compressedPath = String.format("%s/compressed", exampleStudentPath);
            createDirectoryIfNotExists(originalPath, "原图存储目录");
            createDirectoryIfNotExists(compressedPath, "压缩图存储目录");
            
            log.info("照片子目录结构创建完成");
            
        } catch (Exception e) {
            log.error("创建照片子目录失败: {}", e.getMessage(), e);
        }
    }
}
