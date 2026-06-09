package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 商品分类表 — 每个商家可自定义分类
 */
@Entity
@Table(name = "category")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属商家 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    /** 分类名称 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 排序 */
    @Column(nullable = false)
    private Integer sortOrder = 0;
}
