package com.steam.service;

import com.steam.entity.GameReview;
import com.steam.mapper.GameMapper;
import com.steam.mapper.GameReviewMapper;
import com.steam.mapper.UserLibraryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private GameReviewMapper reviewMapper;
    @Mock private GameMapper gameMapper;
    @Mock private UserLibraryMapper userLibraryMapper;
    @Mock private RateLimitService rateLimitService;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewMapper, gameMapper, userLibraryMapper, rateLimitService);
    }

    @Test
    void createReviewThrowsWhenRateLimited() {
        when(userLibraryMapper.existsByUserIdAndGameId(1L, 100L)).thenReturn(true);
        when(reviewMapper.findByUserIdAndGameId(1L, 100L)).thenReturn(null);
        when(rateLimitService.isAllowed("review", 1L, 5, 3600)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(1L, 100L, 5, "Great game", true));
        assertEquals("操作过于频繁，请稍后再试", ex.getMessage());
    }

    @Test
    void createReviewSucceedsWhenNotRateLimited() {
        when(userLibraryMapper.existsByUserIdAndGameId(1L, 100L)).thenReturn(true);
        when(reviewMapper.findByUserIdAndGameId(1L, 100L)).thenReturn(null);
        when(rateLimitService.isAllowed("review", 1L, 5, 3600)).thenReturn(true);
        when(reviewMapper.getAverageRating(100L)).thenReturn(4.5);
        when(reviewMapper.getRatingCount(100L)).thenReturn(10);

        GameReview review = reviewService.createReview(1L, 100L, 5, "Great game", true);
        assertNotNull(review);
        verify(reviewMapper).insert(any(GameReview.class));
    }

    @Test
    void markHelpfulThrowsWhenRateLimited() {
        when(rateLimitService.isAllowed("helpful", null, 10, 60)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.markHelpful(1L, 1L));
        assertEquals("操作过于频繁，请稍后再试", ex.getMessage());
    }

    @Test
    void markHelpfulSucceedsWhenNotRateLimited() {
        when(rateLimitService.isAllowed("helpful", 1L, 10, 60)).thenReturn(true);

        assertDoesNotThrow(() -> reviewService.markHelpful(1L, 1L));
        verify(reviewMapper).incrementHelpful(1L);
    }
}
