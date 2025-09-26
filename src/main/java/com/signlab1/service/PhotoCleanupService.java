package com.signlab1.service;

import com.signlab1.entity.ClassPhoto;
import com.signlab1.mapper.ClassPhotoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 照片清理服务
 * 定期删除过期的原图，保留压缩图
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoCleanupService {
    
    private final ClassPhotoMapper classPhotoMapper;
    
    // 原图保留天数（3天后删除原图）
    private static final int ORIGINAL_PHOTO_RETENTION_DAYS = 3;
    
    // 压缩图保留天数（30天后删除压缩图）
    private static final int COMPRESSED_PHOTO_RETENTION_DAYS = 30;
    
    /**
     * 定期清理过期照片
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredPhotos() {
        log.info("开始执行照片清理任务...");
        
        try {
            // 1. 清理过期的原图
            cleanupExpiredOriginalPhotos();
            
            // 2. 清理过期的压缩图
            cleanupExpiredCompressedPhotos();
            
            log.info("照片清理任务完成");
            
        } catch (Exception e) {
            log.error("照片清理任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 清理过期的原图
     */
    private void cleanupExpiredOriginalPhotos() {
        try {
            // 计算过期时间
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(ORIGINAL_PHOTO_RETENTION_DAYS);
            
            // 查询过期的照片记录
            QueryWrapper<ClassPhoto> query = new QueryWrapper<>();
            query.lt("upload_time", expiredTime)
                 .isNotNull("photo_path")
                 .isNotNull("compressed_photo_path"); // 确保有压缩图才删除原图
            
            List<ClassPhoto> expiredPhotos = classPhotoMapper.selectList(query);
            
            int deletedCount = 0;
            for (ClassPhoto photo : expiredPhotos) {
                if (deleteOriginalPhoto(photo)) {
                    deletedCount++;
                }
            }
            
            log.info("原图清理完成: 查询到{}张过期照片，成功删除{}张原图", expiredPhotos.size(), deletedCount);
            
        } catch (Exception e) {
            log.error("清理过期原图失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 清理过期的压缩图
     */
    private void cleanupExpiredCompressedPhotos() {
        try {
            // 计算过期时间
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(COMPRESSED_PHOTO_RETENTION_DAYS);
            
            // 查询过期的照片记录
            QueryWrapper<ClassPhoto> query = new QueryWrapper<>();
            query.lt("upload_time", expiredTime)
                 .isNotNull("compressed_photo_path");
            
            List<ClassPhoto> expiredPhotos = classPhotoMapper.selectList(query);
            
            int deletedCount = 0;
            for (ClassPhoto photo : expiredPhotos) {
                if (deleteCompressedPhoto(photo)) {
                    deletedCount++;
                    // 同时删除数据库记录
                    classPhotoMapper.deleteById(photo.getId());
                }
            }
            
            log.info("压缩图清理完成: 查询到{}张过期照片，成功删除{}张压缩图和数据库记录", expiredPhotos.size(), deletedCount);
            
        } catch (Exception e) {
            log.error("清理过期压缩图失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 删除原图文件
     */
    private boolean deleteOriginalPhoto(ClassPhoto photo) {
        try {
            if (photo.getPhotoPath() == null || photo.getPhotoPath().trim().isEmpty()) {
                return false;
            }
            
            Path photoPath = Paths.get(photo.getPhotoPath());
            if (Files.exists(photoPath)) {
                Files.delete(photoPath);
                log.debug("原图删除成功: {}", photo.getPhotoPath());
                return true;
            } else {
                log.debug("原图文件不存在: {}", photo.getPhotoPath());
                return false;
            }
            
        } catch (Exception e) {
            log.error("删除原图失败: {}, 错误: {}", photo.getPhotoPath(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除压缩图文件
     */
    private boolean deleteCompressedPhoto(ClassPhoto photo) {
        try {
            if (photo.getCompressedPhotoPath() == null || photo.getCompressedPhotoPath().trim().isEmpty()) {
                return false;
            }
            
            Path compressedPath = Paths.get(photo.getCompressedPhotoPath());
            if (Files.exists(compressedPath)) {
                Files.delete(compressedPath);
                log.debug("压缩图删除成功: {}", photo.getCompressedPhotoPath());
                return true;
            } else {
                log.debug("压缩图文件不存在: {}", photo.getCompressedPhotoPath());
                return false;
            }
            
        } catch (Exception e) {
            log.error("删除压缩图失败: {}, 错误: {}", photo.getCompressedPhotoPath(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 手动清理指定日期的照片
     * @param daysAgo 多少天前的照片
     */
    public void manualCleanup(int daysAgo) {
        log.info("开始手动清理{}天前的照片...", daysAgo);
        
        try {
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(daysAgo);
            
            // 查询指定日期的照片
            QueryWrapper<ClassPhoto> query = new QueryWrapper<>();
            query.lt("upload_time", expiredTime);
            
            List<ClassPhoto> photos = classPhotoMapper.selectList(query);
            
            int originalDeleted = 0;
            int compressedDeleted = 0;
            
            for (ClassPhoto photo : photos) {
                if (deleteOriginalPhoto(photo)) {
                    originalDeleted++;
                }
                if (deleteCompressedPhoto(photo)) {
                    compressedDeleted++;
                }
                // 删除数据库记录
                classPhotoMapper.deleteById(photo.getId());
            }
            
            log.info("手动清理完成: 删除{}张原图，{}张压缩图，{}条数据库记录", 
                    originalDeleted, compressedDeleted, photos.size());
            
        } catch (Exception e) {
            log.error("手动清理失败: {}", e.getMessage(), e);
            throw new RuntimeException("手动清理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取存储统计信息
     */
    public String getStorageStats() {
        try {
            // 统计总照片数
            long totalPhotos = classPhotoMapper.selectCount(null);
            
            // 统计有原图的照片数
            QueryWrapper<ClassPhoto> originalQuery = new QueryWrapper<>();
            originalQuery.isNotNull("photo_path");
            long originalPhotos = classPhotoMapper.selectCount(originalQuery);
            
            // 统计有压缩图的照片数
            QueryWrapper<ClassPhoto> compressedQuery = new QueryWrapper<>();
            compressedQuery.isNotNull("compressed_photo_path");
            long compressedPhotos = classPhotoMapper.selectCount(compressedQuery);
            
            return String.format("存储统计: 总照片数=%d, 有原图=%d, 有压缩图=%d", 
                    totalPhotos, originalPhotos, compressedPhotos);
            
        } catch (Exception e) {
            log.error("获取存储统计失败: {}", e.getMessage(), e);
            return "获取存储统计失败: " + e.getMessage();
        }
    }
}
