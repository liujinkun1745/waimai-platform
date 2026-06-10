package com.waimai.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private User consumer;

    @Column(nullable = false)
    private Integer tasteRating;

    @Column(nullable = false)
    private Integer packagingRating;

    @Column(nullable = false)
    private Integer deliveryRating;

    @Column(length = 500)
    private String comment;

    @Column(length = 500)
    private String reply;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** 综合评分（三维度平均） */
    public double getOverallRating() {
        return (tasteRating + packagingRating + deliveryRating) / 3.0;
    }
}
