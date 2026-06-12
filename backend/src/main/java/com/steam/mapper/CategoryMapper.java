package com.steam.mapper;

import com.steam.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 分类Mapper接口
 */
@Mapper
public interface CategoryMapper {
    
    @Select("SELECT * FROM categories ORDER BY sort_order ASC")
    List<Category> findAll();
    
    @Select("SELECT * FROM categories WHERE id = #{id}")
    Category findById(Long id);
    
    @Select("SELECT c.* FROM categories c " +
            "INNER JOIN game_categories gc ON c.id = gc.category_id " +
            "WHERE gc.game_id = #{gameId}")
    List<Category> findByGameId(Long gameId);
}
