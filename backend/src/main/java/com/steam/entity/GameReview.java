package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 游戏评论实体类
 */
@Data
public class GameReview {
    private Long id;
    private Long userId;
    private Long gameId;
    private Integer rating;
    private String content;
    private Integer isRecommend;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联字段（非数据库字段）
    private User user;
    private Game game;
}
