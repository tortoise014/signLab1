package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.dto.LoginRequest;
import com.signlab1.dto.LoginResponse;
import com.signlab1.dto.SetPasswordRequest;
import com.signlab1.dto.UserStatusDto;
import com.signlab1.entity.User;
import com.signlab1.enums.ResponseCode;
import com.signlab1.exception.BusinessException;
import com.signlab1.mapper.UserMapper;
import com.signlab1.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        
        // 检查是否已设置密码
        if (user.getPasswordSet() == 0) {
            // 未设置密码，返回首次登录标识
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setName(user.getName());
            response.setRole(user.getRole());
            response.setIsFirstLogin(true);
            return response;
        }
        
        // 验证密码（明文比较）
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(400, "请输入密码");
        }
        
        if (!request.getPassword().equals(user.getPassword())) {
            throw new BusinessException(ResponseCode.PASSWORD_ERROR);
        }
        
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setRole(user.getRole());
        response.setToken(token);
        response.setIsFirstLogin(false);
        return response;
    }
    
    /**
     * 设置密码
     */
    public void setPassword(SetPasswordRequest request) {
        // 验证密码格式（6位数字）
        if (request.getPassword() == null || !request.getPassword().matches("\\d{6}")) {
            throw new BusinessException(400, "密码必须是6位数字");
        }
        
        // 验证两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
        
        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        
        // 更新密码（明文保存）
        user.setPassword(request.getPassword());
        user.setPasswordSet(1);
        userMapper.updateById(user);
    }
    
    /**
     * 根据用户编码查询用户信息
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 检查用户是否存在
     */
    public boolean checkUserExists(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 检查用户状态
     */
    public UserStatusDto checkUserStatus(String username) {
        User user = getUserByUsername(username);
        
        UserStatusDto status = new UserStatusDto();
        if (user == null) {
            status.setExists(false);
            status.setPasswordSet(false);
        } else {
            status.setExists(true);
            status.setPasswordSet(user.getPasswordSet() == 1);
            status.setRole(user.getRole());
            status.setName(user.getName());
        }
        
        return status;
    }
}
