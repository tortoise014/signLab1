package com.signlab1.controller;

import com.signlab1.dto.*;
import com.signlab1.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}