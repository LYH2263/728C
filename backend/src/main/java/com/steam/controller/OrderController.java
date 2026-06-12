package com.steam.controller;

import com.steam.dto.CreateOrderDTO;
import com.steam.dto.Result;
import com.steam.entity.Order;
import com.steam.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result<Order> createOrder(HttpServletRequest request, @Valid @RequestBody CreateOrderDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.createOrder(userId, dto.getGameIds());
        return Result.success("订单创建成功", order);
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/{orderNo}/pay")
    public Result<Order> payOrder(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.payOrder(userId, orderNo);
        return Result.success("支付成功", order);
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(userId, orderNo);
        return Result.successMessage("订单已取消");
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping
    public Result<List<Order>> getUserOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Order> orders = orderService.getUserOrders(userId);
        return Result.success(orders);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(HttpServletRequest request, @PathVariable String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getOrderDetail(userId, orderNo);
        return Result.success(order);
    }
}
