package com.steam.service;

import com.steam.entity.CartItem;
import com.steam.entity.Game;
import com.steam.enums.ErrorCode;
import com.steam.exception.BusinessException;
import com.steam.mapper.CartMapper;
import com.steam.mapper.GameMapper;
import com.steam.mapper.UserLibraryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartMapper cartMapper;
    private final GameMapper gameMapper;
    private final UserLibraryMapper userLibraryMapper;
    
    public List<CartItem> getCart(Long userId) {
        return cartMapper.findByUserId(userId);
    }
    
    @Transactional
    public void addToCart(Long userId, Long gameId) {
        Game game = gameMapper.findById(gameId);
        if (game == null) {
            throw BusinessException.of(ErrorCode.GAME_NOT_FOUND);
        }
        
        if (userLibraryMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw BusinessException.of(ErrorCode.GAME_ALREADY_OWNED_GENERIC);
        }
        
        CartItem existItem = cartMapper.findByUserIdAndGameId(userId, gameId);
        if (existItem != null) {
            throw BusinessException.of(ErrorCode.CART_ALREADY_CONTAINS);
        }
        
        if (game.getStock() <= 0) {
            throw BusinessException.of(ErrorCode.GAME_OUT_OF_STOCK_GENERIC);
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setGameId(gameId);
        cartItem.setQuantity(1);
        
        cartMapper.insert(cartItem);
        log.info("用户 {} 添加游戏 {} 到购物车", userId, gameId);
    }
    
    @Transactional
    public void removeFromCart(Long userId, Long gameId) {
        int rows = cartMapper.deleteByUserIdAndGameId(userId, gameId);
        if (rows == 0) {
            throw BusinessException.of(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        log.info("用户 {} 从购物车移除游戏 {}", userId, gameId);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        cartMapper.deleteByUserId(userId);
        log.info("用户 {} 清空购物车", userId);
    }
    
    public int getCartCount(Long userId) {
        return cartMapper.countByUserId(userId);
    }
}
