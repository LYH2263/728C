package com.steam.service;

import com.steam.entity.*;
import com.steam.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private GameMapper gameMapper;
    @Mock private UserMapper userMapper;
    @Mock private CartMapper cartMapper;
    @Mock private UserLibraryMapper userLibraryMapper;
    @Mock private WishlistMapper wishlistMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderMapper, gameMapper, userMapper,
                cartMapper, userLibraryMapper, wishlistMapper);
    }

    @Test
    void createOrderSavesCorrectAmountsToDatabase() {
        User user = new User();
        user.setId(1L);
        when(userMapper.findById(1L)).thenReturn(user);

        Game game = new Game();
        game.setId(100L);
        game.setTitle("Test Game");
        game.setOriginalPrice(new BigDecimal("199.00"));
        game.setDiscountPrice(new BigDecimal("99.00"));
        game.setStock(10);
        when(gameMapper.findById(100L)).thenReturn(game);
        when(userLibraryMapper.existsByUserIdAndGameId(1L, 100L)).thenReturn(false);

        when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return 1;
        });

        Order order = orderService.createOrder(1L, List.of(100L));

        assertEquals(new BigDecimal("199.00"), order.getTotalAmount());
        assertEquals(new BigDecimal("99.00"), order.getPayAmount());
        assertEquals(new BigDecimal("100.00"), order.getDiscountAmount());

        // Verify amounts are saved to DB
        verify(orderMapper).updateAmount(eq(1L), eq(new BigDecimal("199.00")),
                eq(new BigDecimal("99.00")), eq(new BigDecimal("100.00")));
    }

    @Test
    void payOrderRecalculatesPriceFromDatabase() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setUserId(1L);
        order.setPayAmount(new BigDecimal("99.00"));
        order.setStatus("PENDING");
        when(orderMapper.findByOrderNo("ORD001")).thenReturn(order);

        User user = new User();
        user.setId(1L);
        user.setBalance(new BigDecimal("200.00"));
        when(userMapper.findById(1L)).thenReturn(user);

        OrderItem item = new OrderItem();
        item.setGameId(100L);
        item.setGameTitle("Test Game");
        item.setPrice(new BigDecimal("99.00"));
        when(orderMapper.findOrderItemsByOrderId(1L)).thenReturn(List.of(item));

        Game game = new Game();
        game.setId(100L);
        game.setOriginalPrice(new BigDecimal("199.00"));
        game.setDiscountPrice(new BigDecimal("99.00"));
        game.setStock(10);
        when(gameMapper.findById(100L)).thenReturn(game);

        when(gameMapper.decreaseStock(100L)).thenReturn(1);

        Order result = orderService.payOrder(1L, "ORD001");
        assertEquals("PAID", result.getStatus());

        // Balance deducted by recalculated amount
        verify(userMapper).updateBalance(eq(1L), eq(new BigDecimal("101.00")));
    }

    @Test
    void payOrderRejectsWhenPriceChangedAndInsufficientBalance() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setUserId(1L);
        order.setPayAmount(new BigDecimal("50.00"));
        order.setStatus("PENDING");
        when(orderMapper.findByOrderNo("ORD001")).thenReturn(order);

        User user = new User();
        user.setId(1L);
        user.setBalance(new BigDecimal("80.00"));
        when(userMapper.findById(1L)).thenReturn(user);

        OrderItem item = new OrderItem();
        item.setGameId(100L);
        item.setGameTitle("Test Game");
        item.setPrice(new BigDecimal("50.00"));
        when(orderMapper.findOrderItemsByOrderId(1L)).thenReturn(List.of(item));

        Game game = new Game();
        game.setId(100L);
        game.setOriginalPrice(new BigDecimal("199.00"));
        game.setDiscountPrice(null);
        game.setStock(10);
        when(gameMapper.findById(100L)).thenReturn(game);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.payOrder(1L, "ORD001"));
        assertEquals("游戏价格已变动，请重新下单", ex.getMessage());
    }
}
