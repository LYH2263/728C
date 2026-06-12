package com.steam.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private BigDecimal balance;
    private String role;  // USER, ADMIN
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
