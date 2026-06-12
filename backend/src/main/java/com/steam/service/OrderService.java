package com.steam.service;

import com.steam.entity.*;
import com.steam.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderMapper orderMapper;
    private final GameMapper gameMapper;
    private final UserMapper userMapper;
    private final CartMapper cartMapper;
    private final UserLibraryMapper userLibraryMapper;
    private final WishlistMapper wishlistMapper;
    
    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Long userId, List<Long> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) {
            throw new RuntimeException("请选择要购买的游戏");
        }
        
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal payAmount = BigDecimal.ZERO;
        
        // 生成订单号
        String orderNo = generateOrderNo();
        
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus("PENDING");
        
        // 先插入订单获取ID
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        orderMapper.insert(order);
        
        // 处理每个游戏
        for (Long gameId : gameIds) {
            Game game = gameMapper.findById(gameId);
            if (game == null) {
                throw new RuntimeException("游戏不存在: " + gameId);
            }
            
            // 检查是否已购买
            if (userLibraryMapper.existsByUserIdAndGameId(userId, gameId)) {
                throw new RuntimeException("您已拥有游戏: " + game.getTitle());
            }
            
            // 检查库存
            if (game.getStock() <= 0) {
                throw new RuntimeException("游戏库存不足: " + game.getTitle());
            }
            
            BigDecimal price = game.getDiscountPrice() != null ? game.getDiscountPrice() : game.getOriginalPrice();
            totalAmount = totalAmount.add(game.getOriginalPrice());
            payAmount = payAmount.add(price);
            
            // 创建订单明细
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setGameId(gameId);
            orderItem.setGameTitle(game.getTitle());
            orderItem.setGameCover(game.getCoverImage());
            orderItem.setPrice(price);
            orderItem.setQuantity(1);
            orderMapper.insertOrderItem(orderItem);
        }
        
        // 更新订单金额（写回数据库）
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setDiscountAmount(totalAmount.subtract(payAmount));
        orderMapper.updateAmount(order.getId(), totalAmount, payAmount, totalAmount.subtract(payAmount));
        
        log.info("订单创建成功: {}, 用户: {}, 金额: {}", orderNo, userId, payAmount);
        return order;
    }
    
    /**
     * 支付订单
     */
    @Transactional
    public Order payOrder(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }
        
        // 获取订单明细
        List<OrderItem> orderItems = orderMapper.findOrderItemsByOrderId(order.getId());
        
        // 价格二次校验：从数据库重新计算当前价格，防止前端篡改
        BigDecimal recalculatedAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            Game game = gameMapper.findById(item.getGameId());
            if (game == null) {
                throw new RuntimeException("游戏已下架: " + item.getGameTitle());
            }
            BigDecimal currentPrice = game.getDiscountPrice() != null ? game.getDiscountPrice() : game.getOriginalPrice();
            if (currentPrice.compareTo(item.getPrice()) != 0) {
                throw new RuntimeException("游戏价格已变动，请重新下单");
            }
            recalculatedAmount = recalculatedAmount.add(currentPrice);
        }
        
        // 用二次校验后的金额进行余额检查
        User user = userMapper.findById(userId);
        if (user.getBalance().compareTo(recalculatedAmount) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }
        
        // 扣除余额（使用二次校验后的金额）
        userMapper.updateBalance(userId, user.getBalance().subtract(recalculatedAmount));
        
        // 将游戏添加到用户库
        for (OrderItem item : orderItems) {
            // 减少库存
            int rows = gameMapper.decreaseStock(item.getGameId());
            if (rows == 0) {
                throw new RuntimeException("游戏库存不足: " + item.getGameTitle());
            }
            
            // 添加到用户游戏库
            UserLibrary library = new UserLibrary();
            library.setUserId(userId);
            library.setGameId(item.getGameId());
            library.setOrderId(order.getId());
            userLibraryMapper.insert(library);
            
            // 从购物车移除
            cartMapper.deleteByUserIdAndGameId(userId, item.getGameId());
            
            // 从愿望单移除
            wishlistMapper.deleteByUserIdAndGameId(userId, item.getGameId());
        }
        
        // 更新订单状态
        LocalDateTime now = LocalDateTime.now();
        orderMapper.updateStatus(order.getId(), "PAID", now);
        order.setStatus("PAID");
        order.setPayTime(now);
        order.setOrderItems(orderItems);
        
        log.info("订单支付成功: {}, 用户: {}", orderNo, userId);
        return order;
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待支付的订单");
        }
        
        orderMapper.updateStatus(order.getId(), "CANCELLED", null);
        log.info("订单取消成功: {}, 用户: {}", orderNo, userId);
    }
    
    /**
     * 获取用户订单列表
     */
    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.findByUserId(userId);
        for (Order order : orders) {
            order.setOrderItems(orderMapper.findOrderItemsByOrderId(order.getId()));
        }
        return orders;
    }
    
    /**
     * 获取订单详情
     */
    public Order getOrderDetail(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }
        order.setOrderItems(orderMapper.findOrderItemsByOrderId(order.getId()));
        return order;
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}
