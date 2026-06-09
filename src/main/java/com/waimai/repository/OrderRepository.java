package com.waimai.repository;

import com.waimai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问层
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** 按消费者 ID 和状态查找 */
    List<Order> findByConsumerIdAndStatus(Long consumerId, String status);

    /** 按消费者 ID 查找所有订单，按时间倒序 */
    List<Order> findByConsumerIdOrderByCreatedAtDesc(Long consumerId);

    /** 按商家 ID 和状态查找 */
    List<Order> findByMerchantIdAndStatus(Long merchantId, String status);

    /** 按商家 ID 查找所有订单，按时间倒序 */
    List<Order> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    /** 按订单号查找 */
    Optional<Order> findByOrderNo(String orderNo);
}
