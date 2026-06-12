package com.steam.service;

import com.steam.dto.GameQueryDTO;
import com.steam.dto.PageResult;
import com.steam.entity.Category;
import com.steam.entity.Game;
import com.steam.mapper.CategoryMapper;
import com.steam.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 游戏服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameMapper gameMapper;
    private final CategoryMapper categoryMapper;
    
    /**
     * 获取游戏详情
     */
    public Game getGameById(Long id) {
        Game game = gameMapper.findById(id);
        if (game == null) {
            throw new RuntimeException("游戏不存在");
        }
        return game;
    }
    
    /**
     * 获取精选游戏
     */
    public List<Game> getFeaturedGames(Integer limit) {
        return gameMapper.findFeatured(limit != null ? limit : 6);
    }
    
    /**
     * 获取特惠游戏
     */
    public List<Game> getOnSaleGames(Integer limit) {
        return gameMapper.findOnSale(limit != null ? limit : 8);
    }
    
    /**
     * 获取热销游戏
     */
    public List<Game> getBestSellers(Integer limit) {
        return gameMapper.findBestSellers(limit != null ? limit : 10);
    }
    
    /**
     * 获取新品游戏
     */
    public List<Game> getNewReleases(Integer limit) {
        return gameMapper.findNewReleases(limit != null ? limit : 8);
    }
    
    /**
     * 条件查询游戏
     */
    public PageResult<Game> searchGames(GameQueryDTO query) {
        // 解析价格范围
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        if (query.getPriceRange() != null) {
            switch (query.getPriceRange()) {
                case "free":
                    minPrice = BigDecimal.ZERO;
                    maxPrice = BigDecimal.ZERO;
                    break;
                case "under50":
                    maxPrice = new BigDecimal("50");
                    break;
                case "50to100":
                    minPrice = new BigDecimal("50");
                    maxPrice = new BigDecimal("100");
                    break;
                case "100to200":
                    minPrice = new BigDecimal("100");
                    maxPrice = new BigDecimal("200");
                    break;
                case "over200":
                    minPrice = new BigDecimal("200");
                    break;
            }
        }
        
        int offset = (query.getPage() - 1) * query.getSize();
        
        List<Game> games = gameMapper.findByCondition(
                query.getKeyword(),
                query.getCategoryId(),
                minPrice,
                maxPrice,
                query.getOnSale(),
                query.getFeatured(),
                query.getSortBy(),
                query.getSortOrder(),
                offset,
                query.getSize()
        );
        
        Long total = gameMapper.countByCondition(
                query.getKeyword(),
                query.getCategoryId(),
                minPrice,
                maxPrice,
                query.getOnSale(),
                query.getFeatured()
        );
        
        return PageResult.of(games, total, query.getPage(), query.getSize());
    }
    
    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryMapper.findAll();
    }
    
    /**
     * 获取游戏的分类
     */
    public List<Category> getGameCategories(Long gameId) {
        return categoryMapper.findByGameId(gameId);
    }
}
