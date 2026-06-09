package com.waimai.service;

import com.waimai.entity.*;
import com.waimai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务 — 下单、支付、接单、配送、完成、取消
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final BalanceRecordRepository balanceRecordRepository;

    /**
     * 提交订单
     */
    @Transactional
    public Order submitOrder(Long consumerId, Long merchantId, Long addressId,
                              List<CartItem> cartItems, BigDecimal totalAmount) {
        User consumer = userRepository.findById(consumerId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查余额
        if (consumer.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("余额不足，请先充值");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("地址不存在"));

        // 生成订单号
        String orderNo = "WM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6);

        // 创建订单 — 必须用 getReferenceById 获取 JPA 管理的代理对象，而非 builder().id() 游离代理
        Merchant merchantRef = merchantRepository.getReferenceById(merchantId);
        Order order = Order.builder()
                .orderNo(orderNo)
                .consumer(consumer)
                .merchant(merchantRef)
                .addressSnapshot(buildAddressSnapshot(address))
                .totalAmount(totalAmount)
                .status("待接单")
                .build();
        order = orderRepository.save(order);

        // 保存订单商品项
        for (CartItem item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .productImage(item.getProductImage())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // 扣款
        consumer.setBalance(consumer.getBalance().subtract(totalAmount));
        userRepository.save(consumer);

        // 记录消费
        BalanceRecord record = BalanceRecord.builder()
                .user(consumer)
                .amount(totalAmount.negate())
                .type("消费")
                .description("订单支付: " + orderNo)
                .build();
        balanceRecordRepository.save(record);

        return order;
    }

    /** 消费者取消订单 */
    @Transactional
    public void cancelByConsumer(Long orderId, Long consumerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!order.getConsumer().getId().equals(consumerId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"待付款".equals(order.getStatus()) && !"待接单".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可取消");
        }
        // 记录取消前的状态，用于判断是否退款
        boolean wasPaid = !"待付款".equals(order.getStatus());
        order.setStatus("已取消");
        orderRepository.save(order);

        // 退款（如果已支付——即扣过款的状态）
        if (wasPaid) {
            User consumer = order.getConsumer();
            consumer.setBalance(consumer.getBalance().add(order.getTotalAmount()));
            userRepository.save(consumer);

            BalanceRecord record = BalanceRecord.builder()
                    .user(consumer)
                    .amount(order.getTotalAmount())
                    .type("充值")
                    .description("订单退款: " + order.getOrderNo())
                    .build();
            balanceRecordRepository.save(record);
        }
    }

    /** 商家接单 */
    @Transactional
    public void acceptOrder(Long orderId, Long merchantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"待接单".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可接单");
        }
        order.setStatus("待配送");
        order.setAcceptedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /** 商家开始配送 */
    @Transactional
    public void startDelivery(Long orderId, Long merchantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"待配送".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可配送");
        }
        order.setStatus("配送中");
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /** 商家完成订单 */
    @Transactional
    public void completeOrder(Long orderId, Long merchantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!order.getMerchant().getId().equals(merchantId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"配送中".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可完成");
        }
        order.setStatus("已完成");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /** 消费者确认收货 */
    @Transactional
    public void confirmReceived(Long orderId, Long consumerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!order.getConsumer().getId().equals(consumerId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"配送中".equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可确认收货");
        }
        order.setStatus("已完成");
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /** 消费者订单列表 */
    public List<Order> listByConsumer(Long consumerId, String status) {
        if (status == null || status.isBlank()) {
            return orderRepository.findByConsumerIdOrderByCreatedAtDesc(consumerId);
        }
        return orderRepository.findByConsumerIdAndStatus(consumerId, status);
    }

    /** 商家订单列表 */
    public List<Order> listByMerchant(Long merchantId, String status) {
        if (status == null || status.isBlank()) {
            return orderRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
        }
        return orderRepository.findByMerchantIdAndStatus(merchantId, status);
    }

    /** 单条订单 */
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    /** 订单商品项 */
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    /** 批量获取：按订单 ID 分组返回商品项 */
    public Map<Long, List<OrderItem>> getOrderItemsBatch(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return Map.of();
        List<OrderItem> all = orderItemRepository.findByOrderIdIn(orderIds);
        return all.stream().collect(java.util.stream.Collectors.groupingBy(
                item -> item.getOrder().getId()));
    }

    private String buildAddressSnapshot(Address a) {
        return (a.getProvince() != null ? a.getProvince() : "") +
                (a.getCity() != null ? a.getCity() : "") +
                (a.getDistrict() != null ? a.getDistrict() : "") +
                a.getDetailAddress() + " | " + a.getReceiverName() + " " + a.getReceiverPhone();
    }

    /**
     * 购物车项（数据传输用）
     */
    public static class CartItem {
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public CartItem() {}
        public CartItem(Long productId, String productName, String productImage,
                         BigDecimal price, Integer quantity) {
            this.productId = productId;
            this.productName = productName;
            this.productImage = productImage;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}
