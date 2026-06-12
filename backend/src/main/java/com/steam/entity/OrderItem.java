package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 */
@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long gameId;
    private String gameTitle;
    private String gameCover;
    private BigDecimal price;
    private Integer quantity;
    private LocalDateTime createdAt;
}
