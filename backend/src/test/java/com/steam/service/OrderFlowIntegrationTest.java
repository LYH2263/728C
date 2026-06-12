package com.steam.service;

import com.steam.entity.*;
import com.steam.exception.BusinessException;
import com.steam.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("订单核心链路 - 流程集成测试")
class OrderFlowIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private WishlistMapper wishlistMapper;

    @Autowired
    private UserLibraryMapper userLibraryMapper;

    private static final Long TEST_USER_ID = 1L;
    private static final Long POOR_USER_ID = 2L;
    private static final Long RICH_USER_ID = 3L;
    private static final Long GAME_A_ID = 1L;
    private static final Long GAME_B_ID = 2L;
    private static final Long GAME_C_ID = 3L;
    private static final Long GAME_E_ID = 5L;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("完整成功链路：充值→创建订单→支付→余额扣减→游戏入库→库存减少→购物车/愿望单清除→订单PAID")
    void testCompleteOrderFlow_Success() {
        User user = userMapper.findById(TEST_USER_ID);
        BigDecimal initialBalance = user.getBalance();
        assertEquals(BigDecimal.ZERO, initialBalance, "初始余额应为0");

        BigDecimal rechargeAmount = new BigDecimal("500.00");
        userService.updateBalance(TEST_USER_ID, rechargeAmount);
        User afterRecharge = userMapper.findById(TEST_USER_ID);
        assertEquals(0, rechargeAmount.compareTo(afterRecharge.getBalance()),
                "充值后余额应为500");

        Game gameBefore = gameMapper.findById(GAME_E_ID);
        int stockBefore = gameBefore.getStock();
        BigDecimal gamePrice = gameBefore.getDiscountPrice() != null ?
                gameBefore.getDiscountPrice() : gameBefore.getOriginalPrice();

        int cartCountBefore = cartMapper.countByUserId(TEST_USER_ID);
        int wishlistCountBefore = wishlistMapper.countByUserId(TEST_USER_ID);
        boolean inCartBefore = cartMapper.findByUserIdAndGameId(TEST_USER_ID, GAME_E_ID) != null;
        boolean inWishlistBefore = wishlistMapper.existsByUserIdAndGameId(TEST_USER_ID, GAME_E_ID);
        assertTrue(inCartBefore, "游戏E应在购物车中");
        assertTrue(inWishlistBefore, "游戏E应在愿望单中");

        Order order = orderService.createOrder(TEST_USER_ID, List.of(GAME_E_ID));
        assertNotNull(order.getId(), "订单ID不应为空");
        assertEquals("PENDING", order.getStatus(), "订单状态应为PENDING");
        assertEquals(0, gamePrice.compareTo(order.getPayAmount()),
                "订单应付金额应等于游戏价格");

        Order paidOrder = orderService.payOrder(TEST_USER_ID, order.getOrderNo());
        assertEquals("PAID", paidOrder.getStatus(), "支付后订单状态应为PAID");
        assertNotNull(paidOrder.getPayTime(), "支付时间不应为空");

        User afterPay = userMapper.findById(TEST_USER_ID);
        BigDecimal expectedBalance = rechargeAmount.subtract(gamePrice);
        assertEquals(0, expectedBalance.compareTo(afterPay.getBalance()),
                "支付后余额应正确扣减");

        Game gameAfter = gameMapper.findById(GAME_E_ID);
        assertEquals(stockBefore - 1, gameAfter.getStock(),
                "游戏库存应减少1");
        assertEquals(gameBefore.getSalesCount() + 1, gameAfter.getSalesCount(),
                "游戏销量应增加1");

        boolean inLibrary = userLibraryMapper.existsByUserIdAndGameId(TEST_USER_ID, GAME_E_ID);
        assertTrue(inLibrary, "游戏应进入用户游戏库");

        boolean inCartAfter = cartMapper.findByUserIdAndGameId(TEST_USER_ID, GAME_E_ID) != null;
        boolean inWishlistAfter = wishlistMapper.existsByUserIdAndGameId(TEST_USER_ID, GAME_E_ID);
        assertFalse(inCartAfter, "购物车中对应游戏应被清除");
        assertFalse(inWishlistAfter, "愿望单中对应游戏应被清除");

        int cartCountAfter = cartMapper.countByUserId(TEST_USER_ID);
        int wishlistCountAfter = wishlistMapper.countByUserId(TEST_USER_ID);
        assertEquals(cartCountBefore - 1, cartCountAfter, "购物车数量应减少1");
        assertEquals(wishlistCountBefore - 1, wishlistCountAfter, "愿望单数量应减少1");
    }

    @Test
    @DisplayName("失败路径：余额不足时支付应回滚，无副作用")
    void testPayOrder_InsufficientBalance_ShouldRollback() {
        User userBefore = userMapper.findById(POOR_USER_ID);
        BigDecimal balanceBefore = userBefore.getBalance();

        Game gameBefore = gameMapper.findById(GAME_A_ID);
        int stockBefore = gameBefore.getStock();
        BigDecimal gamePrice = gameBefore.getDiscountPrice() != null ?
                gameBefore.getDiscountPrice() : gameBefore.getOriginalPrice();

        assertTrue(balanceBefore.compareTo(gamePrice) < 0,
                "确保余额不足以支付");

        Order order = orderService.createOrder(POOR_USER_ID, List.of(GAME_A_ID));
        assertNotNull(order, "订单应创建成功");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                orderService.payOrder(POOR_USER_ID, order.getOrderNo()));
        assertEquals("余额不足，请先充值", exception.getMessage(),
                "应抛出余额不足异常");

        User userAfter = userMapper.findById(POOR_USER_ID);
        assertEquals(0, balanceBefore.compareTo(userAfter.getBalance()),
                "余额不应变化");

        Game gameAfter = gameMapper.findById(GAME_A_ID);
        assertEquals(stockBefore, gameAfter.getStock(),
                "库存不应减少");

        boolean inLibrary = userLibraryMapper.existsByUserIdAndGameId(POOR_USER_ID, GAME_A_ID);
        assertFalse(inLibrary, "游戏不应进入用户库");

        Order orderAfter = orderMapper.findByOrderNo(order.getOrderNo());
        assertEquals("PENDING", orderAfter.getStatus(),
                "订单状态应保持PENDING");
    }

    @Test
    @DisplayName("失败路径：支付时游戏价格变动应回滚，无副作用")
    void testPayOrder_PriceChanged_ShouldRollback() {
        Long userId = RICH_USER_ID;
        Long gameId = GAME_B_ID;

        User userBefore = userMapper.findById(userId);
        BigDecimal balanceBefore = userBefore.getBalance();

        Game gameBefore = gameMapper.findById(gameId);
        int stockBefore = gameBefore.getStock();

        Order order = orderService.createOrder(userId, List.of(gameId));
        assertNotNull(order, "订单应创建成功");

        gameMapper.updateDiscount(gameId, new BigDecimal("1.00"), 0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.payOrder(userId, order.getOrderNo());
        });
        assertEquals("游戏价格已变动，请重新下单", exception.getMessage(),
                "应抛出价格变动异常");

        User userAfter = userMapper.findById(userId);
        assertEquals(0, balanceBefore.compareTo(userAfter.getBalance()),
                "余额不应扣减");

        Game gameAfter = gameMapper.findById(gameId);
        assertEquals(stockBefore, gameAfter.getStock(),
                "库存不应减少");

        boolean inLibrary = userLibraryMapper.existsByUserIdAndGameId(userId, gameId);
        assertFalse(inLibrary, "游戏不应进入用户库");

        Order orderAfter = orderMapper.findByOrderNo(order.getOrderNo());
        assertEquals("PENDING", orderAfter.getStatus(),
                "订单状态应保持PENDING");
    }

    @Test
    @DisplayName("创建订单时已拥有游戏应报错")
    void testCreateOrder_GameAlreadyOwned_ShouldFail() {
        Long userId = RICH_USER_ID;
        Long gameId = GAME_A_ID;

        Order order = orderService.createOrder(userId, List.of(gameId));
        orderService.payOrder(userId, order.getOrderNo());

        assertTrue(userLibraryMapper.existsByUserIdAndGameId(userId, gameId),
                "游戏应已在库中");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                orderService.createOrder(userId, List.of(gameId)));
        assertTrue(exception.getMessage().contains("您已拥有游戏"),
                "应抛出已拥有游戏异常");
    }

    @Test
    @DisplayName("创建订单时库存为0应报错")
    void testCreateOrder_OutOfStock_ShouldFail() {
        Long userId = RICH_USER_ID;
        Long gameId = 4L;

        Game game = gameMapper.findById(gameId);
        assertEquals(0, game.getStock(), "确保游戏库存为0");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                orderService.createOrder(userId, List.of(gameId)));
        assertTrue(exception.getMessage().contains("库存不足"),
                "应抛出库存不足异常");
    }
}
