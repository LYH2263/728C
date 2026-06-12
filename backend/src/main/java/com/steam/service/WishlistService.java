package com.steam.service;

import com.steam.entity.Game;
import com.steam.entity.Wishlist;
import com.steam.mapper.GameMapper;
import com.steam.mapper.WishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 愿望单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistMapper wishlistMapper;
    private final GameMapper gameMapper;
    
    /**
     * 获取用户愿望单
     */
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistMapper.findByUserId(userId);
    }
    
    /**
     * 添加游戏到愿望单
     */
    @Transactional
    public void addToWishlist(Long userId, Long gameId) {
        // 检查游戏是否存在
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        
        // 检查是否已在愿望单
        if (wishlistMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw new RuntimeException("游戏已在愿望单中");
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setGameId(gameId);
        
        wishlistMapper.insert(wishlist);
        log.info("用户 {} 添加游戏 {} 到愿望单", userId, gameId);
    }
    
    /**
     * 从愿望单移除游戏
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long gameId) {
        int rows = wishlistMapper.deleteByUserIdAndGameId(userId, gameId);
        if (rows == 0) {
            throw new RuntimeException("愿望单中没有该游戏");
        }
        log.info("用户 {} 从愿望单移除游戏 {}", userId, gameId);
    }
    
    /**
     * 检查游戏是否在愿望单中
     */
    public boolean isInWishlist(Long userId, Long gameId) {
        return wishlistMapper.existsByUserIdAndGameId(userId, gameId);
    }
    
    /**
     * 获取愿望单数量
     */
    public int getWishlistCount(Long userId) {
        return wishlistMapper.countByUserId(userId);
    }
}
