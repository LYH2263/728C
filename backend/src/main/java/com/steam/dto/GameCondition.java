package com.steam.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GameCondition {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean onSale;
    private Boolean featured;
    private String sortBy;
    private String sortOrder;
    private Integer offset;
    private Integer limit;
}
