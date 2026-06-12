package com.steam.controller;

import com.steam.dto.GameQueryDTO;
import com.steam.dto.PageResult;
import com.steam.dto.Result;
import com.steam.entity.Category;
import com.steam.entity.Game;
import com.steam.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    /**
     * 获取游戏详情
     */
    @GetMapping("/{id}")
    public Result<Game> getGame(@PathVariable Long id) {
        Game game = gameService.getGameById(id);
        return Result.success(game);
    }
    
    /**
     * 搜索游戏
     */
    @GetMapping("/search")
    public Result<PageResult<Game>> searchGames(GameQueryDTO query) {
        PageResult<Game> result = gameService.searchGames(query);
        return Result.success(result);
    }
    
    /**
     * 获取首页数据
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> getHomeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("featured", gameService.getFeaturedGames(6));
        data.put("onSale", gameService.getOnSaleGames(8));
        data.put("bestSellers", gameService.getBestSellers(10));
        data.put("newReleases", gameService.getNewReleases(8));
        return Result.success(data);
    }
    
    /**
     * 获取精选游戏
     */
    @GetMapping("/featured")
    public Result<List<Game>> getFeaturedGames(@RequestParam(defaultValue = "6") Integer limit) {
        List<Game> games = gameService.getFeaturedGames(limit);
        return Result.success(games);
    }
    
    /**
     * 获取特惠游戏
     */
    @GetMapping("/on-sale")
    public Result<List<Game>> getOnSaleGames(@RequestParam(defaultValue = "8") Integer limit) {
        List<Game> games = gameService.getOnSaleGames(limit);
        return Result.success(games);
    }
    
    /**
     * 获取热销游戏
     */
    @GetMapping("/best-sellers")
    public Result<List<Game>> getBestSellers(@RequestParam(defaultValue = "10") Integer limit) {
        List<Game> games = gameService.getBestSellers(limit);
        return Result.success(games);
    }
    
    /**
     * 获取新品游戏
     */
    @GetMapping("/new-releases")
    public Result<List<Game>> getNewReleases(@RequestParam(defaultValue = "8") Integer limit) {
        List<Game> games = gameService.getNewReleases(limit);
        return Result.success(games);
    }
    
    /**
     * 获取游戏分类
     */
    @GetMapping("/{id}/categories")
    public Result<List<Category>> getGameCategories(@PathVariable Long id) {
        List<Category> categories = gameService.getGameCategories(id);
        return Result.success(categories);
    }
}
