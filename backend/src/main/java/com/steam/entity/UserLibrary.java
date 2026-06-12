package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户游戏库实体类
 */
@Data
public class UserLibrary {
    private Long id;
    private Long userId;
    private Long gameId;
    private Long orderId;
    private Integer playTime;  // 游玩时长（分钟）
    private LocalDateTime lastPlayedAt;
    private LocalDateTime createdAt;
    
    // 关联字段（非数据库字段）
    private Game game;
}
