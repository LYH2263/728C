package com.steam.controller;

import com.steam.dto.LoginDTO;
import com.steam.dto.LoginResponse;
import com.steam.dto.RegisterDTO;
import com.steam.dto.Result;
import com.steam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginDTO dto) {
        LoginResponse response = userService.login(dto);
        return Result.success("登录成功", response);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterDTO dto) {
        LoginResponse response = userService.register(dto);
        return Result.success("注册成功", response);
    }
}
