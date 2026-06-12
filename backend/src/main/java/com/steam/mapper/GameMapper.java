package com.steam.mapper;

import com.steam.dto.GameCondition;
import com.steam.entity.Game;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 游戏Mapper接口
 */
@Mapper
public interface GameMapper {
    
    @Select("SELECT * FROM games WHERE id = #{id} AND status = 1")
    Game findById(Long id);
    
    @Select("SELECT * FROM games WHERE status = 1 ORDER BY created_at DESC")
    List<Game> findAll();
    
    @Select("SELECT * FROM games WHERE status = 1 AND is_featured = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<Game> findFeatured(Integer limit);
    
    @Select("SELECT * FROM games WHERE status = 1 AND discount_percent > 0 ORDER BY discount_percent DESC LIMIT #{limit}")
    List<Game> findOnSale(Integer limit);
    
    @Select("SELECT * FROM games WHERE status = 1 ORDER BY sales_count DESC LIMIT #{limit}")
    List<Game> findBestSellers(Integer limit);
    
    @Select("SELECT * FROM games WHERE status = 1 ORDER BY release_date DESC LIMIT #{limit}")
    List<Game> findNewReleases(Integer limit);
    
    // 复杂查询使用XML配置
    List<Game> findByCondition(@Param("condition") GameCondition condition);
    
    Long countByCondition(@Param("condition") GameCondition condition);
    
    @Update("UPDATE games SET stock = stock - 1, sales_count = sales_count + 1 WHERE id = #{id} AND stock > 0")
    int decreaseStock(Long id);
    
    @Insert("INSERT INTO games (title, description, detail_description, cover_image, original_price, " +
            "discount_price, discount_percent, developer, publisher, release_date, stock, sales_count, " +
            "rating, rating_count, status, is_featured) " +
            "VALUES (#{title}, #{description}, #{detailDescription}, #{coverImage}, #{originalPrice}, " +
            "#{discountPrice}, #{discountPercent}, #{developer}, #{publisher}, #{releaseDate}, #{stock}, " +
            "#{salesCount}, #{rating}, #{ratingCount}, #{status}, #{isFeatured})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Game game);

    @Update("UPDATE games SET discount_price = #{discountPrice}, discount_percent = #{discountPercent}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateDiscount(@Param("id") Long id,
                       @Param("discountPrice") java.math.BigDecimal discountPrice,
                       @Param("discountPercent") Integer discountPercent);

    @Update("UPDATE games SET rating = #{rating}, rating_count = #{ratingCount} WHERE id = #{id}")
    int updateRating(@Param("id") Long id, @Param("rating") java.math.BigDecimal rating, @Param("ratingCount") Integer ratingCount);
}
