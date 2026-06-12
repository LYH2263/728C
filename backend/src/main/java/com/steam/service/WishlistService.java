package com.steam.service;

import com.steam.entity.Game;
import com.steam.entity.Wishlist;
import com.steam.enums.ErrorCode;
import com.steam.exception.BusinessException;
import com.steam.mapper.GameMapper;
import com.steam.mapper.WishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistMapper wishlistMapper;
    private final GameMapper gameMapper;
    
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistMapper.findByUserId(userId);
    }
    
    @Transactional
    public void addToWishlist(Long userId, Long gameId) {
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw BusinessException.of(ErrorCode.GAME_NOT_FOUND);
        }
        
        if (wishlistMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw BusinessException.of(ErrorCode.WISHLIST_ALREADY_CONTAINS);
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setGameId(gameId);
        
        wishlistMapper.insert(wishlist);
        log.info("用户 {} 添加游戏 {} 到愿望单", userId, gameId);
    }
    
    @Transactional
    public void removeFromWishlist(Long userId, Long gameId) {
        int rows = wishlistMapper.deleteByUserIdAndGameId(userId, gameId);
        if (rows == 0) {
            throw BusinessException.of(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        log.info("用户 {} 从愿望单移除游戏 {}", userId, gameId);
    }
    
    public boolean isInWishlist(Long userId, Long gameId) {
        return wishlistMapper.existsByUserIdAndGameId(userId, gameId);
    }
    
    public int getWishlistCount(Long userId) {
        return wishlistMapper.countByUserId(userId);
    }
}
