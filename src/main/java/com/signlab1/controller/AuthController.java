package com.signlab1.controller;

import com.signlab1.dto.*;
import com.signlab1.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success(response, "登录成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 设置密码
     */
    @PostMapping("/set-password")
    public ApiResponse<Void> setPassword(@RequestBody SetPasswordRequest request) {
        try {
            authService.setPassword(request);
            return ApiResponse.success(null, "密码设置成功");
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 检查用户是否存在
     */
    @GetMapping("/check-user/{username}")
    public ApiResponse<Boolean> checkUser(@PathVariable String username) {
        try {
            boolean exists = authService.checkUserExists(username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    /**
     * 检查用户状态（是否存在、是否已设置密码）
     */
    @GetMapping("/check-user-status/{username}")
    public ApiResponse<UserStatusDto> checkUserStatus(@PathVariable String username) {
        try {
            UserStatusDto status = authService.checkUserStatus(username);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}
