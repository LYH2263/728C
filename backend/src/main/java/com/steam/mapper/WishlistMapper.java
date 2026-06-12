package com.steam.mapper;

import com.steam.entity.Wishlist;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 愿望单Mapper接口
 */
@Mapper
public interface WishlistMapper {
    
    @Select("SELECT w.*, g.id as game_id, g.title, g.cover_image, g.original_price, " +
            "g.discount_price, g.discount_percent, g.rating " +
            "FROM wishlist w " +
            "INNER JOIN games g ON w.game_id = g.id " +
            "WHERE w.user_id = #{userId} AND g.status = 1 " +
            "ORDER BY w.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image"),
        @Result(property = "game.originalPrice", column = "original_price"),
        @Result(property = "game.discountPrice", column = "discount_price"),
        @Result(property = "game.discountPercent", column = "discount_percent"),
        @Result(property = "game.rating", column = "rating")
    })
    List<Wishlist> findByUserId(Long userId);
    
    @Select("SELECT * FROM wishlist WHERE user_id = #{userId} AND game_id = #{gameId}")
    Wishlist findByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Insert("INSERT INTO wishlist (user_id, game_id) VALUES (#{userId}, #{gameId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Wishlist wishlist);
    
    @Delete("DELETE FROM wishlist WHERE id = #{id}")
    int deleteById(Long id);
    
    @Delete("DELETE FROM wishlist WHERE user_id = #{userId} AND game_id = #{gameId}")
    int deleteByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Select("SELECT COUNT(*) FROM wishlist WHERE user_id = #{userId}")
    int countByUserId(Long userId);
    
    @Select("SELECT EXISTS(SELECT 1 FROM wishlist WHERE user_id = #{userId} AND game_id = #{gameId})")
    boolean existsByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
}
