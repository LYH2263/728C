package com.steam.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 游戏分类实体类
 */
@Data
public class Category {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
