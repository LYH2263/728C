package com.steam.service;

import com.steam.entity.UserLibrary;
import com.steam.mapper.UserLibraryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户游戏库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLibraryService {
    
    private final UserLibraryMapper userLibraryMapper;
    
    /**
     * 获取用户游戏库
     */
    public List<UserLibrary> getUserLibrary(Long userId) {
        return userLibraryMapper.findByUserId(userId);
    }
    
    /**
     * 检查用户是否拥有游戏
     */
    public boolean ownsGame(Long userId, Long gameId) {
        return userLibraryMapper.existsByUserIdAndGameId(userId, gameId);
    }
    
    /**
     * 获取用户游戏数量
     */
    public int getLibraryCount(Long userId) {
        return userLibraryMapper.countByUserId(userId);
    }
}
