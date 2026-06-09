package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家表 — 店铺信息，与 User 一对一关联
 */
@Entity
@Table(name = "merchant")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的登录用户 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** 店铺名称 */
    @Column(nullable = false, length = 100)
    private String shopName;

    /** 店铺头像 */
    @Column(length = 500)
    private String shopAvatar;

    /** 店铺地址 */
    @Column(nullable = false, length = 255)
    private String shopAddress;

    /** 营业执照号 */
    @Column(nullable = false, length = 50)
    private String businessLicense;

    /** 店铺简介 */
    @Column(length = 500)
    private String description;

    /** 评分 (0-5) */
    @Column(precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.valueOf(4.5);

    /** 月销量 */
    @Column(nullable = false)
    private Integer monthlySales = 0;

    /** 配送费 */
    @Column(precision = 6, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.valueOf(3.00);

    /** 起送价 */
    @Column(precision = 6, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.valueOf(15.00);

    /** 营业时间，如 "09:00-22:00" */
    @Column(length = 50)
    private String businessHours = "09:00-22:00";

    /** 状态：营业中 / 休息中 */
    @Column(length = 10)
    private String status = "营业中";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
