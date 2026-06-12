package com.steam.dto;

import com.steam.entity.User;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private String token;
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private String role;
        
        public static UserInfo fromUser(User user) {
            UserInfo info = new UserInfo();
            info.setId(user.getId());
            info.setUsername(user.getUsername());
            info.setNickname(user.getNickname());
            info.setEmail(user.getEmail());
            info.setAvatar(user.getAvatar());
            info.setRole(user.getRole());
            return info;
        }
    }
}
