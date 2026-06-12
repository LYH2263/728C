package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.User;
import com.steam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public Result<User> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Result<User> updateProfile(HttpServletRequest request, @RequestBody User updateUser) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.updateUser(userId, updateUser);
        return Result.success("更新成功", user);
    }
    
    /**
     * 充值余额
     */
    @PostMapping("/recharge")
    public Result<Void> recharge(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }
        userService.updateBalance(userId, amount);
        return Result.successMessage("充值成功");
    }
}
