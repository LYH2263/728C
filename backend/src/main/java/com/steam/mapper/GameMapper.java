package com.steam.mapper;

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
    List<Game> findByCondition(@Param("keyword") String keyword,
                               @Param("categoryId") Long categoryId,
                               @Param("minPrice") java.math.BigDecimal minPrice,
                               @Param("maxPrice") java.math.BigDecimal maxPrice,
                               @Param("onSale") Boolean onSale,
                               @Param("featured") Boolean featured,
                               @Param("sortBy") String sortBy,
                               @Param("sortOrder") String sortOrder,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);
    
    Long countByCondition(@Param("keyword") String keyword,
                          @Param("categoryId") Long categoryId,
                          @Param("minPrice") java.math.BigDecimal minPrice,
                          @Param("maxPrice") java.math.BigDecimal maxPrice,
                          @Param("onSale") Boolean onSale,
                          @Param("featured") Boolean featured);
    
    @Update("UPDATE games SET stock = stock - 1, sales_count = sales_count + 1 WHERE id = #{id} AND stock > 0")
    int decreaseStock(Long id);
    
    @Update("UPDATE games SET rating = #{rating}, rating_count = #{ratingCount} WHERE id = #{id}")
    int updateRating(@Param("id") Long id, @Param("rating") java.math.BigDecimal rating, @Param("ratingCount") Integer ratingCount);
}
