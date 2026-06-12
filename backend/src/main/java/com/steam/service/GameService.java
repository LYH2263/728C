package com.steam.service;

import com.steam.dto.GameCondition;
import com.steam.dto.GameQueryDTO;
import com.steam.dto.PageResult;
import com.steam.entity.Category;
import com.steam.entity.Game;
import com.steam.enums.ErrorCode;
import com.steam.enums.PriceRange;
import com.steam.exception.BusinessException;
import com.steam.mapper.CategoryMapper;
import com.steam.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameMapper gameMapper;
    private final CategoryMapper categoryMapper;
    
    public Game getGameById(Long id) {
        Game game = gameMapper.findById(id);
        if (game == null) {
            throw BusinessException.of(ErrorCode.GAME_NOT_FOUND);
        }
        return game;
    }
    
    public List<Game> getFeaturedGames(Integer limit) {
        return gameMapper.findFeatured(limit != null ? limit : 6);
    }
    
    public List<Game> getOnSaleGames(Integer limit) {
        return gameMapper.findOnSale(limit != null ? limit : 8);
    }
    
    public List<Game> getBestSellers(Integer limit) {
        return gameMapper.findBestSellers(limit != null ? limit : 10);
    }
    
    public List<Game> getNewReleases(Integer limit) {
        return gameMapper.findNewReleases(limit != null ? limit : 8);
    }
    
    public PageResult<Game> searchGames(GameQueryDTO query) {
        PriceRange priceRange = PriceRange.fromCode(query.getPriceRange());

        GameCondition condition = new GameCondition();
        condition.setKeyword(query.getKeyword());
        condition.setCategoryId(query.getCategoryId());
        if (priceRange != null) {
            condition.setMinPrice(priceRange.getMinPrice());
            condition.setMaxPrice(priceRange.getMaxPrice());
        }
        condition.setOnSale(query.getOnSale());
        condition.setFeatured(query.getFeatured());
        condition.setSortBy(query.getSortBy());
        condition.setSortOrder(query.getSortOrder());

        int offset = (query.getPage() - 1) * query.getSize();
        condition.setOffset(offset);
        condition.setLimit(query.getSize());

        List<Game> games = gameMapper.findByCondition(condition);
        Long total = gameMapper.countByCondition(condition);

        return PageResult.of(games, total, query.getPage(), query.getSize());
    }
    
    public List<Category> getAllCategories() {
        return categoryMapper.findAll();
    }
    
    public List<Category> getGameCategories(Long gameId) {
        return categoryMapper.findByGameId(gameId);
    }
}
