package com.steam.mapper;

import com.steam.entity.Order;
import com.steam.entity.OrderItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper {
    
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order findById(Long id);
    
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);
    
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(Long userId);
    
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND status = #{status} ORDER BY created_at DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Insert("INSERT INTO orders (order_no, user_id, total_amount, pay_amount, discount_amount, status) " +
            "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{payAmount}, #{discountAmount}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
    
    @Update("UPDATE orders SET status = #{status}, pay_time = #{payTime}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("payTime") java.time.LocalDateTime payTime);
    
    @Update("UPDATE orders SET total_amount = #{totalAmount}, pay_amount = #{payAmount}, " +
            "discount_amount = #{discountAmount}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAmount(@Param("id") Long id, @Param("totalAmount") java.math.BigDecimal totalAmount,
                     @Param("payAmount") java.math.BigDecimal payAmount,
                     @Param("discountAmount") java.math.BigDecimal discountAmount);
    
    @Insert("INSERT INTO order_items (order_id, game_id, game_title, game_cover, price, quantity) " +
            "VALUES (#{orderId}, #{gameId}, #{gameTitle}, #{gameCover}, #{price}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrderItem(OrderItem orderItem);
    
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findOrderItemsByOrderId(Long orderId);
}
