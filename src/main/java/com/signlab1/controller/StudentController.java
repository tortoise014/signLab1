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
     * 查看照片（返回照片文件）- 公开访问
     */
    @GetMapping("/photo/{photoId}")
    public ResponseEntity<byte[]> viewPhoto(@PathVariable Long photoId) {
        try {
            // 查询照片信息
            ClassPhoto photo = studentService.getClassPhotoById(photoId);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 读取文件 - 确保使用绝对路径
            Path filePath = Paths.get(photo.getPhotoPath()).toAbsolutePath();
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
     * 生成并下载课堂笔记Word文档
     */
    @GetMapping("/notes/word/{courseId}")
    public ResponseEntity<byte[]> downloadClassNotesWord(@PathVariable String courseId) {
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
            
            // 生成Word文档
            byte[] wordBytes = studentService.generateClassNotesDocument(studentCode, courseId);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            headers.setContentLength(wordBytes.length);
            
            // 生成文件名
            String fileName = "课堂笔记_" + courseId + "_" + studentCode + ".docx";
            headers.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(wordBytes);
                    
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
    
    /**
     * 获取学生的所有课程
     */
    @GetMapping("/courses")
    public ApiResponse<List<CourseInfoDto>> getStudentCourses() {
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
            
            List<CourseInfoDto> courses = studentService.getStudentCourses(studentCode);
            return ApiResponse.success(courses, "获取课程列表成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取课程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 按日期查询学生的课程
     */
    @GetMapping("/courses/date/{date}")
    public ApiResponse<List<CourseInfoDto>> getStudentCoursesByDate(@PathVariable String date) {
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
            
            List<CourseInfoDto> courses = studentService.getStudentCoursesByDate(studentCode, date);
            return ApiResponse.success(courses, "按日期查询课程成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "按日期查询课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取正在进行的课程
     */
    @GetMapping("/current-courses")
    public ResponseEntity<ApiResponse<List<CurrentCourseDto>>> getCurrentCourses() {
        try {
            List<CurrentCourseDto> courses = studentService.getCurrentCourses();
            return ResponseEntity.ok(ApiResponse.success(courses));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取当前课程失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最近的课程
     */
    @GetMapping("/recent-courses")
    public ResponseEntity<ApiResponse<List<RecentCourseDto>>> getRecentCourses() {
        try {
            List<RecentCourseDto> courses = studentService.getRecentCourses();
            return ResponseEntity.ok(ApiResponse.success(courses));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取最近课程失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取最近一次签到记录的课程
     */
    @GetMapping("/last-attendance")
    public ResponseEntity<ApiResponse<LastAttendanceDto>> getLastAttendanceCourse(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.ok(ApiResponse.error(401, "用户信息获取失败，请重新登录"));
            }
            
            String studentCode = authentication.getName();
            if (studentCode == null || studentCode.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error(401, "用户信息获取失败，请重新登录"));
            }
            
            LastAttendanceDto lastAttendance = studentService.getLastAttendanceCourse(studentCode);
            if (lastAttendance == null) {
                return ResponseEntity.ok(ApiResponse.error(404, "暂无签到记录"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(lastAttendance, "获取最近签到记录成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(500, "获取最近签到记录失败: " + e.getMessage()));
        }
    }
}