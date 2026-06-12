package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 购物车项实体类
 */
@Data
public class CartItem {
    private Long id;
    private Long userId;
    private Long gameId;
    private Integer quantity;
    private LocalDateTime createdAt;
    
    // 关联字段（非数据库字段）
    private Game game;
}
