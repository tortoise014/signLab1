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
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response, "登录成功");
    }
    
    /**
     * 设置密码
     */
    @PostMapping("/set-password")
    public ApiResponse<Void> setPassword(@RequestBody SetPasswordRequest request) {
        authService.setPassword(request);
        return ApiResponse.success(null, "密码设置成功");
    }
    
    /**
     * 检查用户是否存在
     */
    @GetMapping("/check-user/{userCode}")
    public ApiResponse<Boolean> checkUser(@PathVariable String userCode) {
        boolean exists = authService.getUserByCode(userCode) != null;
        return ApiResponse.success(exists);
    }
    
    /**
     * 检查用户状态（是否存在、是否已设置密码）
     */
    @GetMapping("/check-user-status/{userCode}")
    public ApiResponse<UserStatusDto> checkUserStatus(@PathVariable String userCode) {
        UserStatusDto status = authService.checkUserStatus(userCode);
        return ApiResponse.success(status);
    }
}
