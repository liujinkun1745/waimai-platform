package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户表 — 统一存储消费者和商家登录信息
 */
@Entity
@Table(name = "`user`")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名（登录用） */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 加密密码 */
    @Column(nullable = false, length = 200)
    private String password;

    /** 手机号（唯一） */
    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    /** 邮箱 */
    @Column(length = 100)
    private String email;

    /** 角色：ROLE_CONSUMER / ROLE_MERCHANT */
    @Column(nullable = false, length = 20)
    private String role;

    /** 账户余额 */
    @Column(precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
