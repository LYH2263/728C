package com.steam.mapper;

import com.steam.entity.CartItem;
import com.steam.entity.Game;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper {
    
    @Select("SELECT ci.*, g.id as game_id, g.title, g.cover_image, g.original_price, " +
            "g.discount_price, g.discount_percent, g.stock " +
            "FROM cart_items ci " +
            "INNER JOIN games g ON ci.game_id = g.id " +
            "WHERE ci.user_id = #{userId} AND g.status = 1")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "quantity", column = "quantity"),
        @Result(property = "game.id", column = "game_id"),
        @Result(property = "game.title", column = "title"),
        @Result(property = "game.coverImage", column = "cover_image"),
        @Result(property = "game.originalPrice", column = "original_price"),
        @Result(property = "game.discountPrice", column = "discount_price"),
        @Result(property = "game.discountPercent", column = "discount_percent"),
        @Result(property = "game.stock", column = "stock")
    })
    List<CartItem> findByUserId(Long userId);
    
    @Select("SELECT * FROM cart_items WHERE user_id = #{userId} AND game_id = #{gameId}")
    CartItem findByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Insert("INSERT INTO cart_items (user_id, game_id, quantity) VALUES (#{userId}, #{gameId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);
    
    @Update("UPDATE cart_items SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Delete("DELETE FROM cart_items WHERE id = #{id}")
    int deleteById(Long id);
    
    @Delete("DELETE FROM cart_items WHERE user_id = #{userId} AND game_id = #{gameId}")
    int deleteByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Delete("DELETE FROM cart_items WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM cart_items WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
