package com.waimai.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表
 */
@Entity
@Table(name = "product")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties({"merchant", "category", "hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属商家 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    /** 所属分类 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 商品名称 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 商品图片 URL */
    @Column(length = 500)
    private String image;

    /** 价格 */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    /** 库存 */
    @Column(nullable = false)
    private Integer stock = 999;

    /** 销量 */
    @Column(nullable = false)
    private Integer sales = 0;

    /** 商品描述 */
    @Column(length = 500)
    private String description;

    /** 状态：上架 / 下架 */
    @Column(length = 5)
    private String status = "上架";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
