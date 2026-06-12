package com.steam.mapper;

import com.steam.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);
    
    @Insert("INSERT INTO users (username, password, email, nickname, avatar, balance, role, status) " +
            "VALUES (#{username}, #{password}, #{email}, #{nickname}, #{avatar}, #{balance}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE users SET nickname = #{nickname}, email = #{email}, avatar = #{avatar}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(User user);
    
    @Update("UPDATE users SET balance = #{balance}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateBalance(@Param("id") Long id, @Param("balance") java.math.BigDecimal balance);
    
    @Update("UPDATE users SET password = #{password}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
