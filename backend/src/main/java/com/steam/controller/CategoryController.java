package com.steam.controller;

import com.steam.dto.Result;
import com.steam.entity.Category;
import com.steam.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final GameService gameService;
    
    /**
     * 获取所有分类
     */
    @GetMapping
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = gameService.getAllCategories();
        return Result.success(categories);
    }
}
