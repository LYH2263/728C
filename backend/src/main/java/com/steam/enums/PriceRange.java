package com.steam.enums;

import java.math.BigDecimal;

public enum PriceRange {

    FREE("free", BigDecimal.ZERO, BigDecimal.ZERO),
    UNDER_50("under50", null, new BigDecimal("50")),
    BETWEEN_50_100("50to100", new BigDecimal("50"), new BigDecimal("100")),
    BETWEEN_100_200("100to200", new BigDecimal("100"), new BigDecimal("200")),
    OVER_200("over200", new BigDecimal("200"), null);

    private final String code;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

    PriceRange(String code, BigDecimal minPrice, BigDecimal maxPrice) {
        this.code = code;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public static PriceRange fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PriceRange range : values()) {
            if (range.code.equalsIgnoreCase(code)) {
                return range;
            }
        }
        throw new IllegalArgumentException("非法的价格区间: " + code);
    }
}
