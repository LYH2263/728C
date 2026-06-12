package com.steam.controller;

import com.steam.dto.PageResult;
import com.steam.dto.Result;
import com.steam.entity.GameReview;
import com.steam.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 获取游戏评论（公开接口）
     */
    @GetMapping("/game/{gameId}")
    public Result<PageResult<GameReview>> getGameReviews(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GameReview> result = reviewService.getGameReviews(gameId, page, size);
        return Result.success(result);
    }
    
    /**
     * 发表评论（需登录）
     */
    @PostMapping
    public Result<GameReview> createReview(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        Long gameId = Long.parseLong(body.get("gameId").toString());
        Integer rating = Integer.parseInt(body.get("rating").toString());
        String content = (String) body.get("content");
        Boolean isRecommend = body.get("isRecommend") != null ? (Boolean) body.get("isRecommend") : true;
        
        GameReview review = reviewService.createReview(userId, gameId, rating, content, isRecommend);
        return Result.success("评论发表成功", review);
    }
    
    /**
     * 标记评论有帮助（需登录）
     */
    @PostMapping("/{reviewId}/helpful")
    public Result<Void> markHelpful(HttpServletRequest request, @PathVariable Long reviewId) {
        Long userId = (Long) request.getAttribute("userId");
        reviewService.markHelpful(userId, reviewId);
        return Result.successMessage("感谢您的反馈");
    }
}
