package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * 订单详情表 — 订单中的每件商品
 */
@Entity
@Table(name = "order_item")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属订单 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** 商品 ID（冗余，用于追溯） */
    private Long productId;

    /** 商品名称（快照） */
    @Column(nullable = false, length = 100)
    private String productName;

    /** 商品图片（快照） */
    @Column(length = 500)
    private String productImage;

    /** 单价（快照） */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    /** 数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 小计 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
