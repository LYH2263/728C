-- 测试数据初始化

-- 测试用户 (密码都是 123456，BCrypt加密)
INSERT INTO users (username, password, email, nickname, avatar, balance, role, status) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'test@steam.com', '测试玩家', '/avatars/default.png', 0.00, 'USER', 1),
('pooruser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'poor@steam.com', '穷玩家', '/avatars/default.png', 10.00, 'USER', 1),
('richuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'rich@steam.com', '土豪玩家', '/avatars/default.png', 10000.00, 'USER', 1);

-- 测试游戏
INSERT INTO games (title, description, detail_description, cover_image, original_price, discount_price, discount_percent, developer, publisher, release_date, stock, sales_count, rating, rating_count, status, is_featured) VALUES
('测试游戏A', '测试游戏A简介', '测试游戏A详细介绍', '/test/game-a.jpg', 199.00, 99.00, 50, 'DevA', 'PubA', '2024-01-01', 100, 0, 4.5, 100, 1, 1),
('测试游戏B', '测试游戏B简介', '测试游戏B详细介绍', '/test/game-b.jpg', 298.00, NULL, 0, 'DevB', 'PubB', '2024-02-01', 50, 0, 4.0, 50, 1, 0),
('测试游戏C', '库存为1的抢购游戏', '超卖测试专用', '/test/game-c.jpg', 99.00, NULL, 0, 'DevC', 'PubC', '2024-03-01', 1, 0, 3.5, 10, 1, 0),
('测试游戏D', '库存为0的缺货游戏', '缺货测试专用', '/test/game-d.jpg', 59.00, NULL, 0, 'DevD', 'PubD', '2024-04-01', 0, 0, 3.0, 5, 1, 0),
('测试游戏E', '低价游戏', '购物车愿望单测试', '/test/game-e.jpg', 29.99, 19.99, 33, 'DevE', 'PubE', '2024-05-01', 200, 0, 4.2, 80, 1, 0);

-- 给 testuser 添加购物车游戏 (游戏A和游戏E)
INSERT INTO cart_items (user_id, game_id, quantity) VALUES
(1, 1, 1),
(1, 5, 1);

-- 给 testuser 添加愿望单游戏 (游戏B和游戏E)
INSERT INTO wishlist (user_id, game_id) VALUES
(1, 2),
(1, 5);
