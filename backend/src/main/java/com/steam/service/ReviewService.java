package com.steam.service;

import com.steam.dto.PageResult;
import com.steam.entity.GameReview;
import com.steam.mapper.GameMapper;
import com.steam.mapper.GameReviewMapper;
import com.steam.mapper.UserLibraryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 评论服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final GameReviewMapper reviewMapper;
    private final GameMapper gameMapper;
    private final UserLibraryMapper userLibraryMapper;
    private final RateLimitService rateLimitService;
    
    /**
     * 获取游戏评论列表
     */
    public PageResult<GameReview> getGameReviews(Long gameId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<GameReview> reviews = reviewMapper.findByGameId(gameId, offset, size);
        Long total = reviewMapper.countByGameId(gameId);
        return PageResult.of(reviews, total, page, size);
    }
    
    /**
     * 发表评论
     */
    @Transactional
    public GameReview createReview(Long userId, Long gameId, Integer rating, String content, Boolean isRecommend) {
        // 检查用户是否拥有游戏
        if (!userLibraryMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw new RuntimeException("您需要购买游戏后才能发表评论");
        }
        
        // 检查是否已评论
        GameReview existReview = reviewMapper.findByUserIdAndGameId(userId, gameId);
        if (existReview != null) {
            throw new RuntimeException("您已经评论过该游戏");
        }
        
        // 验证评分
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }
        
        // 频率限制：每用户每小时最多5条评论
        if (!rateLimitService.isAllowed("review", userId, 5, 3600)) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }
        
        GameReview review = new GameReview();
        review.setUserId(userId);
        review.setGameId(gameId);
        review.setRating(rating);
        review.setContent(content);
        review.setIsRecommend(isRecommend != null && isRecommend ? 1 : 0);
        
        reviewMapper.insert(review);
        
        // 更新游戏评分
        updateGameRating(gameId);
        
        log.info("用户 {} 对游戏 {} 发表评论", userId, gameId);
        return review;
    }
    
    /**
     * 更新评论
     */
    @Transactional
    public GameReview updateReview(Long userId, Long reviewId, Integer rating, String content, Boolean isRecommend) {
        GameReview review = reviewMapper.findByUserIdAndGameId(userId, reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw new RuntimeException("评论不存在或无权修改");
        }
        
        review.setRating(rating);
        review.setContent(content);
        review.setIsRecommend(isRecommend != null && isRecommend ? 1 : 0);
        
        reviewMapper.update(review);
        
        // 更新游戏评分
        updateGameRating(review.getGameId());
        
        return review;
    }
    
    /**
     * 标记评论有帮助
     */
    @Transactional
    public void markHelpful(Long userId, Long reviewId) {
        // 频率限制：每用户每分钟最多10次
        if (!rateLimitService.isAllowed("helpful", userId, 10, 60)) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }
        reviewMapper.incrementHelpful(reviewId);
    }
    
    /**
     * 更新游戏评分
     */
    private void updateGameRating(Long gameId) {
        Double avgRating = reviewMapper.getAverageRating(gameId);
        Integer ratingCount = reviewMapper.getRatingCount(gameId);
        
        if (avgRating != null) {
            BigDecimal rating = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP);
            gameMapper.updateRating(gameId, rating, ratingCount);
        }
    }
}
