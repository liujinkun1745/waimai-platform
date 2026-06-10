package com.waimai.repository;

import com.waimai.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    Optional<Review> findByOrderId(Long orderId);

    long countByMerchantId(Long merchantId);

    @Query("SELECT COALESCE(AVG(r.tasteRating + r.packagingRating + r.deliveryRating) / 3.0, 0) FROM Review r WHERE r.merchant.id = :merchantId")
    double getAverageRating(Long merchantId);
}
