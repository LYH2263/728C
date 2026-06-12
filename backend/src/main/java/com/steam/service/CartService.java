package com.steam.service;

import com.steam.entity.CartItem;
import com.steam.entity.Game;
import com.steam.mapper.CartMapper;
import com.steam.mapper.GameMapper;
import com.steam.mapper.UserLibraryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 购物车服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartMapper cartMapper;
    private final GameMapper gameMapper;
    private final UserLibraryMapper userLibraryMapper;
    
    /**
     * 获取用户购物车
     */
    public List<CartItem> getCart(Long userId) {
        return cartMapper.findByUserId(userId);
    }
    
    /**
     * 添加游戏到购物车
     */
    @Transactional
    public void addToCart(Long userId, Long gameId) {
        // 检查游戏是否存在
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        
        // 检查是否已购买
        if (userLibraryMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw new RuntimeException("您已拥有该游戏");
        }
        
        // 检查是否已在购物车
        CartItem existItem = cartMapper.findByUserIdAndGameId(userId, gameId);
        if (existItem != null) {
            throw new RuntimeException("游戏已在购物车中");
        }
        
        // 检查库存
        if (game.getStock() <= 0) {
            throw new RuntimeException("游戏库存不足");
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setGameId(gameId);
        cartItem.setQuantity(1);
        
        cartMapper.insert(cartItem);
        log.info("用户 {} 添加游戏 {} 到购物车", userId, gameId);
    }
    
    /**
     * 从购物车移除游戏
     */
    @Transactional
    public void removeFromCart(Long userId, Long gameId) {
        int rows = cartMapper.deleteByUserIdAndGameId(userId, gameId);
        if (rows == 0) {
            throw new RuntimeException("购物车中没有该游戏");
        }
        log.info("用户 {} 从购物车移除游戏 {}", userId, gameId);
    }
    
    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
        log.info("用户 {} 清空购物车", userId);
    }
    
    /**
     * 获取购物车商品数量
     */
    public int getCartCount(Long userId) {
        return cartMapper.countByUserId(userId);
    }
}
