package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.CartItem;
import com.steam.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    /**
     * 获取购物车
     */
    @GetMapping
    public Result<List<CartItem>> getCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CartItem> items = cartService.getCart(userId);
        return Result.success(items);
    }
    
    /**
     * 添加到购物车
     */
    @PostMapping
    public Result<Void> addToCart(HttpServletRequest request, @RequestBody Map<String, Long> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long gameId = body.get("gameId");
        cartService.addToCart(userId, gameId);
        return Result.successMessage("添加成功");
    }
    
    /**
     * 从购物车移除
     */
    @DeleteMapping("/{gameId}")
    public Result<Void> removeFromCart(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.removeFromCart(userId, gameId);
        return Result.successMessage("移除成功");
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearCart(userId);
        return Result.successMessage("清空成功");
    }
    
    /**
     * 获取购物车数量
     */
    @GetMapping("/count")
    public Result<Integer> getCartCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = cartService.getCartCount(userId);
        return Result.success(count);
    }
}
