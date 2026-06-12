package com.steam.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO {
    @NotEmpty(message = "购买的游戏不能为空")
    private List<Long> gameIds;
}
