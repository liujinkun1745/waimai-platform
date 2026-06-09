package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额变动记录表
 */
@Entity
@Table(name = "balance_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BalanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 变动金额（正数为充值，负数为消费） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 类型：充值 / 消费 */
    @Column(nullable = false, length = 10)
    private String type;

    /** 描述 */
    @Column(length = 255)
    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
