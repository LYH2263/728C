package com.steam.service;

import com.steam.dto.LoginDTO;
import com.steam.dto.LoginResponse;
import com.steam.dto.RegisterDTO;
import com.steam.entity.User;
import com.steam.enums.ErrorCode;
import com.steam.exception.BusinessException;
import com.steam.mapper.UserMapper;
import com.steam.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public LoginResponse login(LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            throw BusinessException.of(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.of(ErrorCode.WRONG_PASSWORD);
        }
        if (user.getStatus() == 0) {
            throw BusinessException.of(ErrorCode.ACCOUNT_DISABLED);
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(LoginResponse.UserInfo.fromUser(user));
        
        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }
    
    @Transactional
    public LoginResponse register(RegisterDTO dto) {
        if (userMapper.findByUsername(dto.getUsername()) != null) {
            throw BusinessException.of(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (dto.getEmail() != null && userMapper.findByEmail(dto.getEmail()) != null) {
            throw BusinessException.of(ErrorCode.EMAIL_ALREADY_REGISTERED);
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
    
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw BusinessException.of(ErrorCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return user;
    }
    
    @Transactional
    public User updateUser(Long id, User updateUser) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw BusinessException.of(ErrorCode.USER_NOT_FOUND);
        }
        
        if (updateUser.getNickname() != null) {
            user.setNickname(updateUser.getNickname());
        }
        if (updateUser.getEmail() != null) {
            User existUser = userMapper.findByEmail(updateUser.getEmail());
            if (existUser != null && !existUser.getId().equals(id)) {
                throw BusinessException.of(ErrorCode.EMAIL_ALREADY_USED);
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
    
    @Transactional
    public void updateBalance(Long id, BigDecimal amount) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw BusinessException.of(ErrorCode.USER_NOT_FOUND);
        }
        BigDecimal newBalance = user.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_BALANCE_GENERIC);
        }
        userMapper.updateBalance(id, newBalance);
    }
}
