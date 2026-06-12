package com.steam.service;

import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 基于内存的频率限制服务。
 * key = "actionType:userId"，每个 key 维护一个时间戳队列，
 * 在 windowSeconds 窗口内最多允许 maxCount 次操作。
 */
@Service
public class RateLimitService {

    private final Map<String, Deque<Long>> records = new ConcurrentHashMap<>();

    /**
     * @param action        操作类型，如 "review"、"helpful"
     * @param userId        用户ID
     * @param maxCount      窗口内最大允许次数
     * @param windowSeconds 窗口时长（秒）
     * @return true=允许操作，false=被限流
     */
    public boolean isAllowed(String action, Long userId, int maxCount, int windowSeconds) {
        String key = action + ":" + userId;
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000L;

        Deque<Long> timestamps = records.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxCount) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}
