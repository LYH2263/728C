package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.UserLibrary;
import com.steam.service.UserLibraryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏库控制器
 */
@RestController
@RequestMapping("/library")
@RequiredArgsConstructor
public class LibraryController {
    
    private final UserLibraryService libraryService;
    
    /**
     * 获取用户游戏库
     */
    @GetMapping
    public Result<List<UserLibrary>> getLibrary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<UserLibrary> library = libraryService.getUserLibrary(userId);
        return Result.success(library);
    }
    
    /**
     * 检查用户是否拥有游戏
     */
    @GetMapping("/check/{gameId}")
    public Result<Boolean> checkOwnership(HttpServletRequest request, @PathVariable Long gameId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean owns = libraryService.ownsGame(userId, gameId);
        return Result.success(owns);
    }
    
    /**
     * 获取游戏库数量
     */
    @GetMapping("/count")
    public Result<Integer> getLibraryCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = libraryService.getLibraryCount(userId);
        return Result.success(count);
    }
}
