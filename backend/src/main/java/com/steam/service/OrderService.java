package com.steam.service;

import com.steam.entity.*;
import com.steam.enums.ErrorCode;
import com.steam.exception.BusinessException;
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
    
    @Transactional
    public Order createOrder(Long userId, List<Long> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) {
            throw BusinessException.of(ErrorCode.EMPTY_GAME_SELECTION);
        }
        
        User user = userMapper.findById(userId);
        if (user == null) {
            throw BusinessException.of(ErrorCode.USER_NOT_FOUND);
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal payAmount = BigDecimal.ZERO;
        
        String orderNo = generateOrderNo();
        
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus("PENDING");
        
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        orderMapper.insert(order);
        
        for (Long gameId : gameIds) {
            Game game = gameMapper.findById(gameId);
            if (game == null) {
                throw BusinessException.of(ErrorCode.GAME_NOT_FOUND_BY_ID, gameId);
            }
            
            if (userLibraryMapper.existsByUserIdAndGameId(userId, gameId)) {
                throw BusinessException.of(ErrorCode.GAME_ALREADY_OWNED, game.getTitle());
            }
            
            if (game.getStock() <= 0) {
                throw BusinessException.of(ErrorCode.GAME_OUT_OF_STOCK, game.getTitle());
            }
            
            BigDecimal price = game.getDiscountPrice() != null ? game.getDiscountPrice() : game.getOriginalPrice();
            totalAmount = totalAmount.add(game.getOriginalPrice());
            payAmount = payAmount.add(price);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setGameId(gameId);
            orderItem.setGameTitle(game.getTitle());
            orderItem.setGameCover(game.getCoverImage());
            orderItem.setPrice(price);
            orderItem.setQuantity(1);
            orderMapper.insertOrderItem(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setDiscountAmount(totalAmount.subtract(payAmount));
        orderMapper.updateAmount(order.getId(), totalAmount, payAmount, totalAmount.subtract(payAmount));
        
        log.info("订单创建成功: {}, 用户: {}, 金额: {}", orderNo, userId, payAmount);
        return order;
    }
    
    @Transactional
    public Order payOrder(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw BusinessException.of(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.ORDER_NO_PERMISSION);
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw BusinessException.of(ErrorCode.INVALID_ORDER_STATUS);
        }
        
        List<OrderItem> orderItems = orderMapper.findOrderItemsByOrderId(order.getId());
        
        BigDecimal recalculatedAmount = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            Game game = gameMapper.findById(item.getGameId());
            if (game == null) {
                throw BusinessException.of(ErrorCode.GAME_OFF_SHELF, item.getGameTitle());
            }
            BigDecimal currentPrice = game.getDiscountPrice() != null ? game.getDiscountPrice() : game.getOriginalPrice();
            if (currentPrice.compareTo(item.getPrice()) != 0) {
                throw BusinessException.of(ErrorCode.PRICE_CHANGED);
            }
            recalculatedAmount = recalculatedAmount.add(currentPrice);
        }
        
        User user = userMapper.findById(userId);
        if (user.getBalance().compareTo(recalculatedAmount) < 0) {
            throw BusinessException.of(ErrorCode.INSUFFICIENT_BALANCE);
        }
        
        userMapper.updateBalance(userId, user.getBalance().subtract(recalculatedAmount));
        
        for (OrderItem item : orderItems) {
            int rows = gameMapper.decreaseStock(item.getGameId());
            if (rows == 0) {
                throw BusinessException.of(ErrorCode.GAME_OUT_OF_STOCK, item.getGameTitle());
            }
            
            UserLibrary library = new UserLibrary();
            library.setUserId(userId);
            library.setGameId(item.getGameId());
            library.setOrderId(order.getId());
            userLibraryMapper.insert(library);
            
            cartMapper.deleteByUserIdAndGameId(userId, item.getGameId());
            
            wishlistMapper.deleteByUserIdAndGameId(userId, item.getGameId());
        }
        
        LocalDateTime now = LocalDateTime.now();
        orderMapper.updateStatus(order.getId(), "PAID", now);
        order.setStatus("PAID");
        order.setPayTime(now);
        order.setOrderItems(orderItems);
        
        log.info("订单支付成功: {}, 用户: {}", orderNo, userId);
        return order;
    }
    
    @Transactional
    public void cancelOrder(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw BusinessException.of(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.ORDER_NO_PERMISSION);
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw BusinessException.of(ErrorCode.CANCEL_NON_PENDING);
        }
        
        orderMapper.updateStatus(order.getId(), "CANCELLED", null);
        log.info("订单取消成功: {}, 用户: {}", orderNo, userId);
    }
    
    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.findByUserId(userId);
        for (Order order : orders) {
            order.setOrderItems(orderMapper.findOrderItemsByOrderId(order.getId()));
        }
        return orders;
    }
    
    public Order getOrderDetail(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw BusinessException.of(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.ORDER_VIEW_NO_PERMISSION);
        }
        order.setOrderItems(orderMapper.findOrderItemsByOrderId(order.getId()));
        return order;
    }
    
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}
