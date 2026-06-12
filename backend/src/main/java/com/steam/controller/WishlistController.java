package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.Wishlist;
import com.steam.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 愿望单控制器
 */
@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    /**
     * 获取愿望单
     */
    @GetMapping
    public Result<List<Wishlist>> getWishlist(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Wishlist> items = wishlistService.getWishlist(userId);
        return Result.success(items);
    }
    
    /**
     * 添加到愿望单
     */
    @PostMapping
    public Result<Void> addToWishlist(HttpServletRequest request, @RequestBody Map<String, Long> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long gameId = body.get("gameId");
        wishlistService.addToWishlist(userId, gameId);
        return Result.successMessage("添加成功");
    }
    
    /**
     * 从愿望单移除
     */
    @DeleteMapping("/{gameId}")
    public Result<Void> removeFromWishlist(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        wishlistService.removeFromWishlist(userId, gameId);
        return Result.successMessage("移除成功");
    }
    
    /**
     * 检查游戏是否在愿望单中
     */
    @GetMapping("/check/{gameId}")
    public Result<Boolean> checkWishlist(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean inWishlist = wishlistService.isInWishlist(userId, gameId);
        return Result.success(inWishlist);
    }
}
