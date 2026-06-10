package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Entity
@Table(name = "`order`")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单号（唯一） */
    @Column(nullable = false, unique = true, length = 30)
    private String orderNo;

    /** 下单消费者 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private User consumer;

    /** 商家 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    /** 收货地址（JSON 快照或关联 ID） */
    @Column(length = 500)
    private String addressSnapshot;

    /** 订单总金额 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** 状态：待付款 / 待接单 / 待配送 / 配送中 / 已完成 / 已取消 */
    @Column(nullable = false, length = 10)
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** 动态计算预计送达时间（不存数据库），已取消订单返回 null */
    public LocalDateTime getEstimatedDelivery() {
        if ("已取消".equals(status)) return null;
        if (completedAt != null) return completedAt;
        if (deliveredAt != null) return deliveredAt.plusMinutes(10);
        if (acceptedAt != null) return acceptedAt.plusMinutes(25);
        return createdAt != null ? createdAt.plusMinutes(30) : LocalDateTime.now().plusMinutes(30);
    }

    /** 获取当前进度的步骤号 (0=待接单, 1=待配送, 2=配送中, 3=已完成, -1=已取消) */
    public int getStep() {
        return switch (status) {
            case "待接单" -> 0;
            case "待配送" -> 1;
            case "配送中" -> 2;
            case "已完成" -> 3;
            default -> -1;
        };
    }
}
