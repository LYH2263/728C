package com.steam.service;

import com.steam.dto.LoginDTO;
import com.steam.dto.LoginResponse;
import com.steam.dto.RegisterDTO;
import com.steam.entity.User;
import com.steam.mapper.UserMapper;
import com.steam.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(LoginResponse.UserInfo.fromUser(user));
        
        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public LoginResponse register(RegisterDTO dto) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱是否已存在
        if (dto.getEmail() != null && userMapper.findByEmail(dto.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setAvatar("/avatars/default.png");
        user.setBalance(BigDecimal.ZERO);
        user.setRole("USER");
        user.setStatus(1);
        
        userMapper.insert(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(LoginResponse.UserInfo.fromUser(user));
        
        log.info("用户注册成功: {}", user.getUsername());
        return response;
    }
    
    /**
     * 获取用户信息
     */
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, User updateUser) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            User existUser = userMapper.findByEmail(updateUser.getEmail());
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
            user.setEmail(updateUser.getEmail());
        }
        if (updateUser.getAvatar() != null) {
            user.setAvatar(updateUser.getAvatar());
        }
        
        userMapper.update(user);
        user.setPassword(null);
        return user;
    }
    
    /**
     * 更新用户余额
     */
    @Transactional
    public void updateBalance(Long id, BigDecimal amount) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        BigDecimal newBalance = user.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("余额不足");
        }
        userMapper.updateBalance(id, newBalance);
    }
}
