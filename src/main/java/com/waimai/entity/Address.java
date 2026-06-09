package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 收货地址表
 */
@Entity
@Table(name = "address")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属消费者 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private User consumer;

    /** 收货人姓名 */
    @Column(nullable = false, length = 50)
    private String receiverName;

    /** 收货人电话 */
    @Column(nullable = false, length = 20)
    private String receiverPhone;

    /** 省 */
    @Column(length = 20)
    private String province;

    /** 市 */
    @Column(length = 20)
    private String city;

    /** 区 */
    @Column(length = 20)
    private String district;

    /** 详细地址 */
    @Column(nullable = false, length = 255)
    private String detailAddress;

    /** 是否默认地址 */
    @Column(nullable = false)
    private Boolean isDefault = false;
}
