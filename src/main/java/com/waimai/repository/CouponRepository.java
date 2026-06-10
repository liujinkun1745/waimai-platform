package com.waimai.repository;

import com.waimai.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByUserIdAndUsedFalse(Long userId);
    List<Coupon> findByUserId(Long userId);
    long countByUserIdAndUsedFalse(Long userId);
}
