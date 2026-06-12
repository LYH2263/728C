package com.steam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void allowsFirstAction() {
        assertTrue(rateLimitService.isAllowed("review", 1L, 5, 60));
    }

    @Test
    void allowsActionsWithinLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitService.isAllowed("review", 1L, 5, 60));
        }
    }

    @Test
    void blocksActionsExceedingLimit() {
        for (int i = 0; i < 5; i++) {
            rateLimitService.isAllowed("review", 1L, 5, 60);
        }
        assertFalse(rateLimitService.isAllowed("review", 1L, 5, 60));
    }

    @Test
    void tracksUsersIndependently() {
        for (int i = 0; i < 5; i++) {
            rateLimitService.isAllowed("review", 1L, 5, 60);
        }
        assertFalse(rateLimitService.isAllowed("review", 1L, 5, 60));
        assertTrue(rateLimitService.isAllowed("review", 2L, 5, 60));
    }

    @Test
    void tracksActionTypesIndependently() {
        for (int i = 0; i < 5; i++) {
            rateLimitService.isAllowed("review", 1L, 5, 60);
        }
        assertFalse(rateLimitService.isAllowed("review", 1L, 5, 60));
        assertTrue(rateLimitService.isAllowed("helpful", 1L, 5, 60));
    }

    @Test
    void allowsActionsAfterWindowExpires() throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            rateLimitService.isAllowed("review", 1L, 2, 1);
        }
        assertFalse(rateLimitService.isAllowed("review", 1L, 2, 1));

        Thread.sleep(1100);
        assertTrue(rateLimitService.isAllowed("review", 1L, 2, 1));
    }
}
