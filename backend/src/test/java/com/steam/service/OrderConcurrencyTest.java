package com.steam.service;

import com.steam.entity.Game;
import com.steam.entity.Order;
import com.steam.entity.User;
import com.steam.exception.BusinessException;
import com.steam.mapper.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("订单核心链路 - 并发安全测试")
class OrderConcurrencyTest {

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
    private UserLibraryMapper userLibraryMapper;

    private static final int CONCURRENT_THREADS = 10;

    @Test
    @DisplayName("并发安全：同一用户对同一PENDING订单并发重复支付，不应重复扣款、重复入库")
    void testConcurrentPayment_SameOrder_ShouldNotDoubleCharge() throws Exception {
        Long userId = createTestUser("concurrentUser1", new BigDecimal("1000.00"));
        Long gameId = createTestGame("并发测试游戏A", new BigDecimal("99.00"), 100);

        Order order = orderService.createOrder(userId, java.util.List.of(gameId));
        String orderNo = order.getOrderNo();
        assertNotNull(orderNo);

        User beforePay = userMapper.findById(userId);
        BigDecimal balanceBefore = beforePay.getBalance();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_THREADS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    orderService.payOrder(userId, orderNo);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(),
                "只能有1次支付成功，其余应失败");
        assertEquals(CONCURRENT_THREADS - 1, failCount.get(),
                "其余" + (CONCURRENT_THREADS - 1) + "次应失败");

        User afterPay = userMapper.findById(userId);
        BigDecimal expectedBalance = balanceBefore.subtract(new BigDecimal("99.00"));
        assertEquals(0, expectedBalance.compareTo(afterPay.getBalance()),
                "余额只能被扣减一次");

        int libraryCount = userLibraryMapper.countByUserId(userId);
        assertEquals(1, libraryCount,
                "游戏只能入库一次");

        Game gameAfter = gameMapper.findById(gameId);
        assertEquals(99, gameAfter.getStock(),
                "库存只能减少1");

        Order orderAfter = orderMapper.findByOrderNo(orderNo);
        assertEquals("PAID", orderAfter.getStatus(),
                "订单最终状态应为PAID");
    }

    @Test
    @DisplayName("并发安全：多用户抢购stock=1的游戏，不应出现超卖（库存为负）")
    void testConcurrentPurchase_OneStock_ShouldNotOversell() throws Exception {
        Long gameId = createTestGame("限量抢购游戏", new BigDecimal("299.00"), 1);

        int userCount = 5;
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            Long userId = createTestUser("buyer" + i, new BigDecimal("1000.00"));
            userIds.add(userId);
        }

        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch readyLatch = new CountDownLatch(userCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> successOrderNos = Collections.synchronizedList(new ArrayList<>());
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < userCount; i++) {
            final Long userId = userIds.get(i);
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    Order order = orderService.createOrder(userId, java.util.List.of(gameId));
                    orderService.payOrder(userId, order.getOrderNo());
                    successCount.incrementAndGet();
                    successOrderNos.add(order.getOrderNo());
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        Game gameAfter = gameMapper.findById(gameId);
        assertTrue(gameAfter.getStock() >= 0,
                "库存不能为负数（不能超卖），当前库存: " + gameAfter.getStock());
        assertEquals(0, gameAfter.getStock(),
                "限量1份的游戏，库存最终应为0");

        assertEquals(1, successCount.get(),
                "只能有1位用户抢购成功");
        assertEquals(userCount - 1, failCount.get(),
                "其余用户应失败（库存不足）");

        int totalLibraryCount = 0;
        for (Long userId : userIds) {
            totalLibraryCount += userLibraryMapper.countByUserId(userId);
        }
        assertEquals(1, totalLibraryCount,
                "总共只能有1份游戏入库");
    }

    @Test
    @DisplayName("并发安全：多用户抢购多库存游戏，库存和销量应准确")
    void testConcurrentPurchase_MultipleStock_ShouldBeAccurate() throws Exception {
        int initialStock = 10;
        int userCount = 10;
        Long gameId = createTestGame("批量抢购游戏", new BigDecimal("50.00"), initialStock);

        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            Long userId = createTestUser("bulkBuyer" + i, new BigDecimal("500.00"));
            userIds.add(userId);
        }

        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch readyLatch = new CountDownLatch(userCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(userCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < userCount; i++) {
            final Long userId = userIds.get(i);
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    Order order = orderService.createOrder(userId, java.util.List.of(gameId));
                    orderService.payOrder(userId, order.getOrderNo());
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        Game gameAfter = gameMapper.findById(gameId);
        assertTrue(gameAfter.getStock() >= 0,
                "库存不能为负数");

        int expectedSold = initialStock - gameAfter.getStock();
        assertEquals(expectedSold, successCount.get(),
                "成功购买人数应等于减少的库存数");

        assertEquals(initialStock, gameAfter.getStock() + gameAfter.getSalesCount(),
                "剩余库存 + 销量 = 初始库存");

        int totalLibraryCount = 0;
        for (Long userId : userIds) {
            totalLibraryCount += userLibraryMapper.countByUserId(userId);
        }
        assertEquals(successCount.get(), totalLibraryCount,
                "入库游戏数应等于成功购买人数");
    }

    private Long createTestUser(String username, BigDecimal balance) {
        User user = new User();
        user.setUsername(username + System.currentTimeMillis());
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
        user.setEmail(username + System.currentTimeMillis() + "@test.com");
        user.setNickname(username);
        user.setBalance(balance);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        return user.getId();
    }

    private Long createTestGame(String title, BigDecimal price, int stock) {
        Game game = new Game();
        game.setTitle(title + System.currentTimeMillis());
        game.setDescription("测试游戏");
        game.setOriginalPrice(price);
        game.setDiscountPrice(null);
        game.setDiscountPercent(0);
        game.setDeveloper("TestDev");
        game.setPublisher("TestPub");
        game.setStock(stock);
        game.setSalesCount(0);
        game.setStatus(1);
        game.setIsFeatured(0);
        gameMapper.insert(game);
        return game.getId();
    }
}
