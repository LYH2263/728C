package com.steam.service;

import com.steam.dto.PageResult;
import com.steam.entity.GameReview;
import com.steam.enums.ErrorCode;
import com.steam.exception.BusinessException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final GameReviewMapper reviewMapper;
    private final GameMapper gameMapper;
    private final UserLibraryMapper userLibraryMapper;
    private final RateLimitService rateLimitService;
    
    public PageResult<GameReview> getGameReviews(Long gameId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<GameReview> reviews = reviewMapper.findByGameId(gameId, offset, size);
        Long total = reviewMapper.countByGameId(gameId);
        return PageResult.of(reviews, total, page, size);
    }
    
    @Transactional
    public GameReview createReview(Long userId, Long gameId, Integer rating, String content, Boolean isRecommend) {
        if (!userLibraryMapper.existsByUserIdAndGameId(userId, gameId)) {
            throw BusinessException.of(ErrorCode.REVIEW_NOT_PURCHASED);
        }
        
        GameReview existReview = reviewMapper.findByUserIdAndGameId(userId, gameId);
        if (existReview != null) {
            throw BusinessException.of(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        
        if (rating < 1 || rating > 5) {
            throw BusinessException.of(ErrorCode.INVALID_RATING);
        }
        
        if (!rateLimitService.isAllowed("review", userId, 5, 3600)) {
            throw BusinessException.of(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        
        GameReview review = new GameReview();
        review.setUserId(userId);
        review.setGameId(gameId);
        review.setRating(rating);
        review.setContent(content);
        review.setIsRecommend(isRecommend != null && isRecommend ? 1 : 0);
        
        reviewMapper.insert(review);
        
        updateGameRating(gameId);
        
        log.info("用户 {} 对游戏 {} 发表评论", userId, gameId);
        return review;
    }
    
    @Transactional
    public GameReview updateReview(Long userId, Long reviewId, Integer rating, String content, Boolean isRecommend) {
        GameReview review = reviewMapper.findById(reviewId);
        if (review == null || !review.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.REVIEW_NO_PERMISSION);
        }
        
        review.setRating(rating);
        review.setContent(content);
        review.setIsRecommend(isRecommend != null && isRecommend ? 1 : 0);
        
        reviewMapper.update(review);
        
        updateGameRating(review.getGameId());
        
        return review;
    }
    
    @Transactional
    public void markHelpful(Long userId, Long reviewId) {
        if (!rateLimitService.isAllowed("helpful", userId, 10, 60)) {
            throw BusinessException.of(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        reviewMapper.incrementHelpful(reviewId);
    }
    
    private void updateGameRating(Long gameId) {
        Double avgRating = reviewMapper.getAverageRating(gameId);
        Integer ratingCount = reviewMapper.getRatingCount(gameId);
        
        if (avgRating != null) {
            BigDecimal rating = BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP);
            gameMapper.updateRating(gameId, rating, ratingCount);
        }
    }
}
