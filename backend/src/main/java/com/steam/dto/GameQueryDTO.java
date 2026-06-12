package com.steam.dto;

import lombok.Data;

/**
 * 游戏查询条件DTO
 */
@Data
public class GameQueryDTO {
    private String keyword;       // 搜索关键词
    private Long categoryId;      // 分类ID
    private String priceRange;    // 价格范围：free, under50, 50to100, 100to200, over200
    private String sortBy;        // 排序字段：price, rating, sales, releaseDate
    private String sortOrder;     // 排序方向：asc, desc
    private Integer page = 1;     // 页码
    private Integer size = 12;    // 每页数量
    private Boolean onSale;       // 是否打折
    private Boolean featured;     // 是否精选
}
