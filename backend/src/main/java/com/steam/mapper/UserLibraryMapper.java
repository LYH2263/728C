package com.steam.mapper;

import com.steam.entity.UserLibrary;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 用户游戏库Mapper接口
 */
@Mapper
public interface UserLibraryMapper {
    
    @Select("SELECT ul.*, g.id as game_id, g.title, g.cover_image, g.developer " +
            "FROM user_library ul " +
            "INNER JOIN games g ON ul.game_id = g.id " +
            "WHERE ul.user_id = #{userId} " +
            "ORDER BY ul.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "playTime", column = "play_time"),
        @Result(property = "lastPlayedAt", column = "last_played_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image"),
        @Result(property = "game.developer", column = "developer")
    })
    List<UserLibrary> findByUserId(Long userId);
    
    @Select("SELECT * FROM user_library WHERE user_id = #{userId} AND game_id = #{gameId}")
    UserLibrary findByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Insert("INSERT INTO user_library (user_id, game_id, order_id) VALUES (#{userId}, #{gameId}, #{orderId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserLibrary userLibrary);
    
    @Update("UPDATE user_library SET play_time = #{playTime}, last_played_at = #{lastPlayedAt} " +
            "WHERE user_id = #{userId} AND game_id = #{gameId}")
    int updatePlayTime(@Param("userId") Long userId, @Param("gameId") Long gameId, 
                       @Param("playTime") Integer playTime, @Param("lastPlayedAt") java.time.LocalDateTime lastPlayedAt);
    
    @Select("SELECT COUNT(*) FROM user_library WHERE user_id = #{userId}")
    int countByUserId(Long userId);
    
    @Select("SELECT EXISTS(SELECT 1 FROM user_library WHERE user_id = #{userId} AND game_id = #{gameId})")
    boolean existsByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
}
