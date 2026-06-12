package com.steam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Steam游戏平台启动类
 */
@SpringBootApplication
@MapperScan("com.steam.mapper")
public class SteamApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SteamApplication.class, args);
    }
}
