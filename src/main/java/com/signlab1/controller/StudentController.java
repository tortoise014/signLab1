package com.signlab1.controller;

import com.signlab1.dto.*;
import com.signlab1.entity.ClassPhoto;
import com.signlab1.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 学生端API控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {
    
    private final StudentService studentService;
    
    /**
     * 扫码签到
     */
    @PostMapping("/attendance/scan")
    public ApiResponse<AttendanceResultDto> scanAttendance(@RequestBody ScanAttendanceRequest request) {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentCode = authentication.getName();
            
            AttendanceResultDto result = studentService.scanAttendance(studentCode, request.getQrData());
            return ApiResponse.success(result, "签到成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "签到失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到记录
     */
    @GetMapping("/attendance/records")
    public ApiResponse<List<AttendanceRecordDto>> getAttendanceRecords() {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentCode = authentication.getName();
            
            List<AttendanceRecordDto> records = studentService.getAttendanceRecords(studentCode);
            return ApiResponse.success(records, "获取签到记录成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取签到记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    @GetMapping("/attendance/stats")
    public ApiResponse<AttendanceStatsDto> getAttendanceStats() {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String studentCode = authentication.getName();
            
            AttendanceStatsDto stats = studentService.getAttendanceStats(studentCode);
            return ApiResponse.success(stats, "获取签到统计成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取签到统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 绑定班级
     */
    @PostMapping("/bind-class")
    public ApiResponse<Void> bindClass(@RequestBody BindClassRequest request) {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ApiResponse.error(401, "未登录，请先登录");
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ApiResponse.error(401, "用户信息获取失败，请重新登录");
            }
            
            studentService.bindClass(studentCode, request.getVerificationCode());
            return ApiResponse.success(null, "绑定班级成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "绑定班级失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取已绑定的班级列表
     */
    @GetMapping("/classes")
    public ApiResponse<List<ClassInfoDto>> getStudentClasses() {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ApiResponse.error(401, "未登录，请先登录");
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ApiResponse.error(401, "用户信息获取失败，请重新登录");
            }
            
            List<ClassInfoDto> classes = studentService.getStudentClasses(studentCode);
            return ApiResponse.success(classes, "获取班级列表成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取班级列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传课堂照片
     */
    @PostMapping("/photo/upload")
    public ApiResponse<PhotoUploadResponse> uploadClassPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") String courseId,
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ApiResponse.error(401, "未登录，请先登录");
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ApiResponse.error(401, "用户信息获取失败，请重新登录");
            }
            
            PhotoUploadResponse response = studentService.uploadClassPhoto(studentCode, courseId, file, remark);
            return ApiResponse.success(response, "照片上传成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "照片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生的课堂照片列表
     */
    @GetMapping("/photos")
    public ApiResponse<List<ClassPhotoDto>> getStudentPhotos() {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ApiResponse.error(401, "未登录，请先登录");
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ApiResponse.error(401, "用户信息获取失败，请重新登录");
            }
            
            List<ClassPhotoDto> photos = studentService.getStudentPhotos(studentCode);
            return ApiResponse.success(photos, "获取照片列表成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取照片列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID获取学生的课堂照片
     */
    @GetMapping("/photos/course/{courseId}")
    public ApiResponse<List<ClassPhotoDto>> getStudentPhotosByCourse(@PathVariable String courseId) {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ApiResponse.error(401, "未登录，请先登录");
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ApiResponse.error(401, "用户信息获取失败，请重新登录");
            }
            
            List<ClassPhotoDto> photos = studentService.getStudentPhotosByCourse(studentCode, courseId);
            return ApiResponse.success(photos, "获取课程照片成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取课程照片失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除课堂照片
     */
    @DeleteMapping("/photo/{photoId}")
    public ApiResponse<Void> deleteClassPhoto(@PathVariable Long photoId) {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ApiResponse.error(401, "未登录，请先登录");
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ApiResponse.error(401, "用户信息获取失败，请重新登录");
            }
            
            studentService.deleteClassPhoto(studentCode, photoId);
            return ApiResponse.success(null, "照片删除成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "照片删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看照片（返回照片文件）
     */
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<byte[]> viewPhoto(@PathVariable Long photoId) {
        try {
            // 从SecurityContext获取当前登录学生的学号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ResponseEntity.status(401).build();
            }
            
            // 查询照片信息
            ClassPhoto photo = studentService.getClassPhotoById(photoId);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 验证权限（只能查看自己的照片）
            if (!studentCode.equals(photo.getStudentUsername())) {
                return ResponseEntity.status(403).build();
            }
            
            // 读取文件
            Path filePath = Paths.get(photo.getPhotoPath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(photo.getPhotoName());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileContent.length);
            headers.set("Content-Disposition", "inline; filename=\"" + photo.getPhotoName() + "\"");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据文件名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
}