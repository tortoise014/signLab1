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
     * 检查当前用户是否为教师角色
     */
    private ApiResponse<String> checkTeacherPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ApiResponse.error(401, "未登录，请先登录");
        }
        
        // 检查用户角色是否为教师
        boolean isTeacher = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TEACHER"));
        
        if (!isTeacher) {
            return ApiResponse.error(403, "权限不足，只有教师可以访问此功能");
        }
        
        String teacherUsername = authentication.getName();
        if (teacherUsername == null || teacherUsername.isEmpty()) {
            return ApiResponse.error(401, "用户信息获取失败，请重新登录");
        }
        
        return ApiResponse.success(teacherUsername, "权限验证通过");
    }
    
    /**
     * 获取老师今日课程
     */
    @GetMapping("/courses")
    public ApiResponse<List<CourseInfoDto>> getTodayCourses() {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<CourseInfoDto> courses = teacherService.getTodayCourses(teacherUsername);
            
            // 如果没有课程，返回友好的提示信息
            if (courses.isEmpty()) {
                return ApiResponse.success(courses, "您今天没有课程哦");
            }
            
            return ApiResponse.success(courses, "获取今日课程成功，共" + courses.size() + "门课程");
        } catch (Exception e) {
            // 根据异常类型返回不同的错误信息
            if (e.getMessage().contains("权限不足")) {
                return ApiResponse.error(403, "权限不足，无法访问该资源");
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<CourseInfoDto> courses = teacherService.getAllCourses(teacherUsername);
            
            if (courses.isEmpty()) {
                return ApiResponse.success(courses, "暂无课程安排");
            }
            
            return ApiResponse.success(courses, "获取所有课程成功，共" + courses.size() + "门课程");
        } catch (Exception e) {
            if (e.getMessage().contains("权限不足")) {
                return ApiResponse.error(403, "权限不足，无法访问该资源");
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
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
            if (e.getMessage().contains("权限不足")) {
                return ApiResponse.error(403, "权限不足，无法访问该资源");
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            AttendanceQrDto qrDto = teacherService.generateAttendanceQr(courseId);
            return ApiResponse.success(qrDto, "生成二维码成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "生成二维码失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成通用签到二维码（支持跨班签到）
     */
    @PostMapping("/attendance/qr/universal")
    public ApiResponse<AttendanceQrDto> generateUniversalAttendanceQr(@RequestParam String courseId) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            AttendanceQrDto qrDto = teacherService.generateUniversalAttendanceQr(courseId);
            return ApiResponse.success(qrDto, "生成通用二维码成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "生成通用二维码失败: " + e.getMessage());
        }
    }
    
    /**
     * 配置多班级课程
     */
    @PostMapping("/multi-class/configure")
    public ApiResponse<Void> configureMultiClassCourse(@RequestBody MultiClassCourseRequest request) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            teacherService.configureMultiClassCourse(request);
            return ApiResponse.success(null, "配置多班级课程成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "配置多班级课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取多班级课程信息
     */
    @GetMapping("/multi-class/info/{courseId}")
    public ApiResponse<MultiClassCourseInfoDto> getMultiClassCourseInfo(@PathVariable String courseId) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            MultiClassCourseInfoDto info = teacherService.getMultiClassCourseInfo(courseId);
            return ApiResponse.success(info, "获取多班级课程信息成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取多班级课程信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除多班级课程配置
     */
    @DeleteMapping("/multi-class/{courseId}")
    public ApiResponse<Void> deleteMultiClassCourse(@PathVariable String courseId) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            teacherService.deleteMultiClassCourse(courseId);
            return ApiResponse.success(null, "删除多班级课程配置成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "删除多班级课程配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    @GetMapping("/attendance/stats")
    public ApiResponse<AttendanceStatsDto> getAttendanceStats(@RequestParam String courseId) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
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
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ResponseEntity.status(permissionCheck.getCode()).build();
            }
            
            byte[] templateBytes = excelTemplateService.generateStudentTemplate();
            
            if (templateBytes == null || templateBytes.length == 0) {
                // 返回错误信息，但保持文件下载格式
                String errorMsg = "模板生成失败，请稍后重试";
                byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.set("Content-Disposition", "attachment; filename=\"error.txt\"");
                
                return ResponseEntity.badRequest()
                        .headers(headers)
                        .body(errorBytes);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            String encodedFileName = URLEncoder.encode("学生模板.xlsx", StandardCharsets.UTF_8);
            headers.set("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(templateBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误日志
            
            // 返回错误信息文件
            String errorMsg = "下载学生模板失败: " + e.getMessage();
            byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.set("Content-Disposition", "attachment; filename=\"error.txt\"");
            
            return ResponseEntity.internalServerError()
                    .headers(headers)
                    .body(errorBytes);
        }
    }
    
    /**
     * 下载课程模板
     */
    @GetMapping("/template/courses")
    public ResponseEntity<byte[]> downloadCourseTemplate() {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ResponseEntity.status(permissionCheck.getCode()).build();
            }
            
            byte[] templateBytes = excelTemplateService.generateCourseTemplate();
            
            if (templateBytes == null || templateBytes.length == 0) {
                // 返回错误信息，但保持文件下载格式
                String errorMsg = "模板生成失败，请稍后重试";
                byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.set("Content-Disposition", "attachment; filename=\"error.txt\"");
                
                return ResponseEntity.badRequest()
                        .headers(headers)
                        .body(errorBytes);
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            String encodedFileName = URLEncoder.encode("课程模板.xlsx", StandardCharsets.UTF_8);
            headers.set("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentLength(templateBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误日志
            
            // 返回错误信息文件
            String errorMsg = "下载课程模板失败: " + e.getMessage();
            byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.set("Content-Disposition", "attachment; filename=\"error.txt\"");
            
            return ResponseEntity.internalServerError()
                    .headers(headers)
                    .body(errorBytes);
        }
    }
    
    /**
     * 从学生数据生成课程模板
     */
    @PostMapping("/template/courses-from-student")
    public ResponseEntity<byte[]> generateCourseTemplateFromStudent(@RequestParam("file") MultipartFile file) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ResponseEntity.status(permissionCheck.getCode()).build();
            }
            
            String teacherCode = permissionCheck.getData();
            
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
     * 获取老师的学生列表（支持过滤条件）
     */
    @GetMapping("/students")
    public ApiResponse<List<StudentInfoDto>> getTeacherStudents(
            @RequestParam(value = "classCode", required = false) String classCode,
            @RequestParam(value = "studentType", required = false) String studentType) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<StudentInfoDto> students = teacherService.getTeacherStudents(teacherUsername, classCode, studentType);
            
            // 统计信息
            long classStudentCount = students.stream()
                    .filter(s -> "CLASS_STUDENT".equals(s.getStudentType()))
                    .count();
            long crossClassCount = students.stream()
                    .filter(s -> "CROSS_CLASS_ATTENDEE".equals(s.getStudentType()))
                    .count();
            
            String message = String.format("获取学生列表成功，共%d名学生（本班学生：%d名，跨班签到学生：%d名）", 
                    students.size(), classStudentCount, crossClassCount);
            
            return ApiResponse.success(students, message);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取老师的学生列表（简化版，保持向后兼容）
     */
    @GetMapping("/students/all")
    public ApiResponse<List<StudentInfoDto>> getAllTeacherStudents() {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<StudentInfoDto> students = teacherService.getTeacherStudents(teacherUsername);
            
            // 添加调试信息
            long classStudentCount = students.stream()
                    .filter(s -> "CLASS_STUDENT".equals(s.getStudentType()))
                    .count();
            long crossClassCount = students.stream()
                    .filter(s -> "CROSS_CLASS_ATTENDEE".equals(s.getStudentType()))
                    .count();
            
            String message = String.format("获取学生列表成功，共%d名学生（本班学生：%d名，跨班签到学生：%d名）", 
                    students.size(), classStudentCount, crossClassCount);
            
            return ApiResponse.success(students, message);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取老师的学生列表（详细版，包含统计信息）
     */
    @GetMapping("/students/detailed")
    public ApiResponse<TeacherStudentListDto> getTeacherStudentsDetailed(
            @RequestParam(value = "classCode", required = false) String classCode,
            @RequestParam(value = "studentType", required = false) String studentType) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<StudentInfoDto> students = teacherService.getTeacherStudents(teacherUsername, classCode, studentType);
            
            // 构建统计信息
            TeacherStudentListDto.StudentStats stats = new TeacherStudentListDto.StudentStats();
            stats.setTotalCount(students.size());
            
            long classStudentCount = students.stream()
                    .filter(s -> "CLASS_STUDENT".equals(s.getStudentType()))
                    .count();
            stats.setClassStudentCount((int) classStudentCount);
            
            long crossClassCount = students.stream()
                    .filter(s -> "CROSS_CLASS_ATTENDEE".equals(s.getStudentType()))
                    .count();
            stats.setCrossClassAttendeeCount((int) crossClassCount);
            
            long attendedCount = students.stream()
                    .filter(s -> s.getAttendanceStatus() != null && s.getAttendanceStatus() > 0)
                    .count();
            stats.setAttendedCount((int) attendedCount);
            
            stats.setNotAttendedCount(students.size() - (int) attendedCount);
            
            // 计算本班学生签到率
            if (classStudentCount > 0) {
                long classAttendedCount = students.stream()
                        .filter(s -> "CLASS_STUDENT".equals(s.getStudentType()) 
                                && s.getAttendanceStatus() != null && s.getAttendanceStatus() > 0)
                        .count();
                double rate = (double) classAttendedCount / classStudentCount * 100;
                stats.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
            } else {
                stats.setAttendanceRate(0.0);
            }
            
            // 构建响应
            TeacherStudentListDto result = new TeacherStudentListDto();
            result.setStudents(students);
            result.setStats(stats);
            
            return ApiResponse.success(result, "获取学生列表成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取老师班级中的学生列表（仅本班学生）
     */
    @GetMapping("/class-students")
    public ApiResponse<List<StudentInfoDto>> getTeacherClassStudents() {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<StudentInfoDto> students = teacherService.getTeacherClassStudents(teacherUsername);
            
            // 统计信息
            long attendedCount = students.stream()
                    .filter(s -> s.getAttendanceStatus() != null && s.getAttendanceStatus() > 0)
                    .count();
            long notAttendedCount = students.size() - attendedCount;
            
            String message = String.format("获取本班学生列表成功，共%d名学生（已签到：%d名，未签到：%d名）", 
                    students.size(), attendedCount, notAttendedCount);
            
            return ApiResponse.success(students, message);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取本班学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取跨班签到学生列表（不是本班学生但已签到）
     */
    @GetMapping("/cross-class-attendees")
    public ApiResponse<List<StudentInfoDto>> getCrossClassAttendeeStudents() {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            List<StudentInfoDto> students = teacherService.getCrossClassAttendeeStudents(teacherUsername);
            
            String message = String.format("获取跨班签到学生列表成功，共%d名学生", students.size());
            
            return ApiResponse.success(students, message);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取跨班签到学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 调试接口：获取老师学生列表的详细信息
     */
    @GetMapping("/students/debug")
    public ApiResponse<String> debugTeacherStudents() {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            String teacherUsername = permissionCheck.getData();
            
            // 获取调试信息
            String debugInfo = teacherService.getTeacherStudentsDebugInfo(teacherUsername);
            
            return ApiResponse.success(debugInfo, "调试信息获取成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取调试信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取详细签到统计
     */
    @GetMapping("/attendance/detailed-stats/{courseId}")
    public ApiResponse<DetailedAttendanceStatsDto> getDetailedAttendanceStats(@PathVariable String courseId) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            DetailedAttendanceStatsDto stats = teacherService.getDetailedAttendanceStats(courseId);
            return ApiResponse.success(stats, "获取详细签到统计成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "获取详细签到统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学生信息
     */
    @PutMapping("/students/update")
    public ApiResponse<Void> updateStudent(@RequestBody UpdateStudentRequest request) {
        try {
            // 检查教师权限
            ApiResponse<String> permissionCheck = checkTeacherPermission();
            if (!permissionCheck.isSuccess()) {
                return ApiResponse.error(permissionCheck.getCode(), permissionCheck.getMessage());
            }
            
            teacherService.updateStudent(request);
            return ApiResponse.success(null, "学生信息更新成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "学生信息更新失败: " + e.getMessage());
        }
    }
}