package com.signlab1.controller;

import com.signlab1.dto.ApiResponse;
import com.signlab1.dto.AttendanceQrDto;
import com.signlab1.dto.AttendanceStatsDto;
import com.signlab1.dto.CourseInfoDto;
import com.signlab1.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 页面控制器
 */
@Controller
@RequiredArgsConstructor
public class PageController {
    
    private final TeacherService teacherService;
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    /**
     * 设置密码页面
     */
    @GetMapping("/set-password")
    public String setPassword() {
        return "set-password";
    }
    
    /**
     * 老师端首页
     */
    @GetMapping("/teacher")
    public String teacherHome(@RequestParam String teacherCode, Model model) {
        try {
            List<CourseInfoDto> courses = teacherService.getTodayCourses(teacherCode);
            model.addAttribute("courses", courses);
            model.addAttribute("teacherCode", teacherCode);
            return "teacher-home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 签到页面
     */
    @GetMapping("/teacher/attendance")
    public String attendance(@RequestParam String courseId, Model model) {
        try {
            AttendanceQrDto qrDto = teacherService.generateAttendanceQr(courseId);
            AttendanceStatsDto stats = teacherService.getAttendanceStats(courseId);
            
            model.addAttribute("qrDto", qrDto);
            model.addAttribute("stats", stats);
            model.addAttribute("courseId", courseId);
            return "attendance";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 学生端首页
     */
    @GetMapping("/student")
    public String studentHome(@RequestParam String studentCode, Model model) {
        try {
            model.addAttribute("studentCode", studentCode);
            return "student-home";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * 管理员页面
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
