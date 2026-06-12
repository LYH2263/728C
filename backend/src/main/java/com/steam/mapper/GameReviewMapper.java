package com.steam.mapper;

import com.steam.entity.GameReview;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 游戏评论Mapper接口
 */
@Mapper
public interface GameReviewMapper {
    
    @Select("SELECT gr.*, u.username, u.nickname, u.avatar " +
            "FROM game_reviews gr " +
            "INNER JOIN users u ON gr.user_id = u.id " +
            "WHERE gr.game_id = #{gameId} " +
            "ORDER BY gr.helpful_count DESC, gr.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "gameId", column = "game_id"),
        @Result(property = "rating", column = "rating"),
        @Result(property = "content", column = "content"),
        @Result(property = "isRecommend", column = "is_recommend"),
        @Result(property = "helpfulCount", column = "helpful_count"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "user.id", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.nickname", column = "nickname"),
        @Result(property = "user.avatar", column = "avatar")
    })
    List<GameReview> findByGameId(@Param("gameId") Long gameId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    @Select("SELECT COUNT(*) FROM game_reviews WHERE game_id = #{gameId}")
    Long countByGameId(Long gameId);
    
    @Select("SELECT * FROM game_reviews WHERE user_id = #{userId} AND game_id = #{gameId}")
    GameReview findByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Insert("INSERT INTO game_reviews (user_id, game_id, rating, content, is_recommend) " +
            "VALUES (#{userId}, #{gameId}, #{rating}, #{content}, #{isRecommend})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GameReview review);
    
    @Update("UPDATE game_reviews SET rating = #{rating}, content = #{content}, " +
            "is_recommend = #{isRecommend}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(GameReview review);
    
    @Update("UPDATE game_reviews SET helpful_count = helpful_count + 1 WHERE id = #{id}")
    int incrementHelpful(Long id);
    
    @Delete("DELETE FROM game_reviews WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT AVG(rating) FROM game_reviews WHERE game_id = #{gameId}")
    Double getAverageRating(Long gameId);
    
    @Select("SELECT COUNT(*) FROM game_reviews WHERE game_id = #{gameId}")
    Integer getRatingCount(Long gameId);
}
