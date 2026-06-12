package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 游戏实体类
 */
@Data
public class Game {
    private Long id;
    private String title;
    private String description;
    private String detailDescription;
    private String coverImage;
    private String bannerImage;
    private String screenshots;  // JSON数组
    private String videoUrl;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private Integer discountPercent;
    private String developer;
    private String publisher;
    private LocalDate releaseDate;
    private String minRequirements;  // JSON
    private String recRequirements;  // JSON
    private String tags;  // JSON数组
    private Integer stock;
    private Integer salesCount;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer status;
    private Integer isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
