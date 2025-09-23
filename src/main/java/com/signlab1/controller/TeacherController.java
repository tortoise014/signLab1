package com.signlab1.controller;

import com.signlab1.dto.*;
import com.signlab1.service.TeacherService;
import com.signlab1.service.AdminImportService;
import com.signlab1.service.ExcelTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 老师端API控制器
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeacherController {
    
    private final TeacherService teacherService;
    private final AdminImportService adminImportService;
    private final ExcelTemplateService excelTemplateService;
    
    /**
     * 获取老师今日课程
     */
    @GetMapping("/courses")
    public ApiResponse<List<CourseInfoDto>> getTodayCourses() {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherUsername = authentication.getName();
            
            List<CourseInfoDto> courses = teacherService.getTodayCourses(teacherUsername);
            
            // 如果没有课程，返回友好的提示信息
            if (courses.isEmpty()) {
                return ApiResponse.success(courses, "您今天没有课程哦");
            }
            
            return ApiResponse.success(courses, "获取今日课程成功，共" + courses.size() + "门课程");
        } catch (Exception e) {
            // 根据异常类型返回不同的错误信息
            if (e.getMessage().contains("用户不存在")) {
                return ApiResponse.error(404, "教师用户不存在");
            } else if (e.getMessage().contains("数据库")) {
                return ApiResponse.error(500, "数据库查询异常，请稍后重试");
            } else {
                return ApiResponse.error(500, "获取课程失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取老师所有课程
     */
    @GetMapping("/courses/all")
    public ApiResponse<List<CourseInfoDto>> getAllCourses() {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherUsername = authentication.getName();
            
            List<CourseInfoDto> courses = teacherService.getAllCourses(teacherUsername);
            
            if (courses.isEmpty()) {
                return ApiResponse.success(courses, "暂无课程安排");
            }
            
            return ApiResponse.success(courses, "获取所有课程成功，共" + courses.size() + "门课程");
        } catch (Exception e) {
            if (e.getMessage().contains("用户不存在")) {
                return ApiResponse.error(404, "教师用户不存在");
            } else if (e.getMessage().contains("数据库")) {
                return ApiResponse.error(500, "数据库查询异常，请稍后重试");
            } else {
                return ApiResponse.error(500, "获取课程失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 根据日期获取老师课程
     */
    @GetMapping("/courses/by-date")
    public ApiResponse<List<CourseInfoDto>> getCoursesByDate(@RequestParam String date) {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherUsername = authentication.getName();
            
            // 验证日期格式
            if (date == null || date.trim().isEmpty()) {
                return ApiResponse.error(400, "日期参数不能为空");
            }
            
            List<CourseInfoDto> courses = teacherService.getCoursesByDate(teacherUsername, date);
            
            if (courses.isEmpty()) {
                return ApiResponse.success(courses, "您" + date + "没有课程哦");
            }
            
            return ApiResponse.success(courses, "获取" + date + "课程成功，共" + courses.size() + "门课程");
        } catch (Exception e) {
            if (e.getMessage().contains("用户不存在")) {
                return ApiResponse.error(404, "教师用户不存在");
            } else if (e.getMessage().contains("日期格式")) {
                return ApiResponse.error(400, "日期格式错误，请使用yyyy-MM-dd格式");
            } else if (e.getMessage().contains("数据库")) {
                return ApiResponse.error(500, "数据库查询异常，请稍后重试");
            } else {
                return ApiResponse.error(500, "获取课程失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 生成签到二维码
     */
    @PostMapping("/attendance/qr")
    public ApiResponse<AttendanceQrDto> generateAttendanceQr(@RequestParam String courseId) {
        try {
            AttendanceQrDto qrDto = teacherService.generateAttendanceQr(courseId);
            return ApiResponse.success(qrDto, "生成二维码成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "生成二维码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    @GetMapping("/attendance/stats")
    public ApiResponse<AttendanceStatsDto> getAttendanceStats(@RequestParam String courseId) {
        try {
            AttendanceStatsDto stats = teacherService.getAttendanceStats(courseId);
            return ApiResponse.success(stats, "获取统计成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未签到学生名单
     */
    @GetMapping("/attendance/absent-students")
    public ApiResponse<List<String>> getAbsentStudents(@RequestParam String courseId) {
        try {
            List<String> absentStudents = teacherService.getAbsentStudents(courseId);
            return ApiResponse.success(absentStudents, "获取未签到学生成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取未签到学生失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看课程签到情况
     */
    @GetMapping("/attendance/course/{courseId}")
    public ApiResponse<List<StudentAttendanceDto>> getCourseAttendance(@PathVariable String courseId) {
        try {
            List<StudentAttendanceDto> attendance = teacherService.getCourseAttendance(courseId);
            return ApiResponse.success(attendance, "获取课程签到情况成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取课程签到情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改学生签到状态
     */
    @PutMapping("/attendance/update")
    public ApiResponse<Void> updateStudentAttendance(@RequestBody UpdateAttendanceRequest request) {
        try {
            teacherService.updateStudentAttendance(request.getCourseId(), request.getStudentCode(), request.getStatus());
            return ApiResponse.success(null, "签到状态更新成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "签到状态更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入学生数据
     */
    @PostMapping("/import/students")
    public ApiResponse<String> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherUsername = authentication.getName();
            
            String result = adminImportService.importStudentsForTeacher(file, teacherUsername);
            return ApiResponse.success(result, "学生数据导入成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "学生数据导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入课程数据
     */
    @PostMapping("/import/courses")
    public ApiResponse<String> importCourses(@RequestParam("file") MultipartFile file) {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherUsername = authentication.getName();
            
            String result = adminImportService.importCoursesForTeacher(file, teacherUsername);
            return ApiResponse.success(result, "课程数据导入成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "课程数据导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载学生模板
     */
    @GetMapping("/template/students")
    public ResponseEntity<byte[]> downloadStudentTemplate() {
        try {
            byte[] templateBytes = excelTemplateService.generateStudentTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            String encodedFileName = URLEncoder.encode("学生模板.xlsx", StandardCharsets.UTF_8);
            headers.set("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(templateBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 下载课程模板
     */
    @GetMapping("/template/courses")
    public ResponseEntity<byte[]> downloadCourseTemplate() {
        try {
            byte[] templateBytes = excelTemplateService.generateCourseTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            String encodedFileName = URLEncoder.encode("课程模板.xlsx", StandardCharsets.UTF_8);
            headers.set("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(templateBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 从学生数据生成课程模板
     */
    @PostMapping("/template/courses-from-student")
    public ResponseEntity<byte[]> generateCourseTemplateFromStudent(@RequestParam("file") MultipartFile file) {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherCode = authentication.getName();
            
            byte[] templateData = excelTemplateService.generateCourseTemplateFromStudentData(file, teacherCode);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            
            String encodedFileName = URLEncoder.encode("从学生数据生成的课程模板.xlsx", StandardCharsets.UTF_8);
            headers.set("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(templateData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(templateData);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取老师的学生列表
     */
    @GetMapping("/students")
    public ApiResponse<List<StudentInfoDto>> getTeacherStudents() {
        try {
            // 从SecurityContext获取当前登录老师的工号
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String teacherUsername = authentication.getName();
            
            List<StudentInfoDto> students = teacherService.getTeacherStudents(teacherUsername);
            return ApiResponse.success(students, "获取学生列表成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学生信息
     */
    @PutMapping("/students/update")
    public ApiResponse<Void> updateStudent(@RequestBody UpdateStudentRequest request) {
        try {
            teacherService.updateStudent(request);
            return ApiResponse.success(null, "学生信息更新成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "学生信息更新失败: " + e.getMessage());
        }
    }
}