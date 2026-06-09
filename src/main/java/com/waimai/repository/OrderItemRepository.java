package com.waimai.repository;

import com.waimai.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 订单详情数据访问层
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** 按订单 ID 查找所有商品项 */
    List<OrderItem> findByOrderId(Long orderId);

    /** 批量查询：按多个订单 ID 查找所有商品项 */
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
}
