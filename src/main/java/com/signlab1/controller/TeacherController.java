package com.signlab1.controller;

import com.signlab1.dto.ApiResponse;
import com.signlab1.dto.AttendanceQrDto;
import com.signlab1.dto.AttendanceStatsDto;
import com.signlab1.dto.CourseInfoDto;
import com.signlab1.service.TeacherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 老师端控制器
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {
    
    private final TeacherService teacherService;
    
    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }
    
    /**
     * 获取今日课程
     */
    @GetMapping("/courses/today")
    public ApiResponse<List<CourseInfoDto>> getTodayCourses(@RequestParam String teacherCode) {
        try {
            List<CourseInfoDto> courses = teacherService.getTodayCourses(teacherCode);
            return ApiResponse.success(courses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 根据日期获取课程
     */
    @GetMapping("/courses")
    public ApiResponse<List<CourseInfoDto>> getCoursesByDate(
            @RequestParam String teacherCode, 
            @RequestParam String date) {
        try {
            List<CourseInfoDto> courses = teacherService.getCoursesByDate(teacherCode, date);
            return ApiResponse.success(courses);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 生成签到二维码
     */
    @PostMapping("/attendance/qr")
    public ApiResponse<AttendanceQrDto> generateAttendanceQr(@RequestParam String courseId) {
        try {
            AttendanceQrDto qrDto = teacherService.generateAttendanceQr(courseId);
            return ApiResponse.success(qrDto);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    @GetMapping("/attendance/stats")
    public ApiResponse<AttendanceStatsDto> getAttendanceStats(@RequestParam String courseId) {
        try {
            AttendanceStatsDto stats = teacherService.getAttendanceStats(courseId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取未签到学生名单
     */
    @GetMapping("/attendance/absent")
    public ApiResponse<List<String>> getAbsentStudents(@RequestParam String courseId) {
        try {
            List<String> absentStudents = teacherService.getAbsentStudents(courseId);
            return ApiResponse.success(absentStudents);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
