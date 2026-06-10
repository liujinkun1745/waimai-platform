package com.waimai.service;

import com.waimai.entity.Coupon;
import com.waimai.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<Coupon> getAvailable(Long userId) {
        return couponRepository.findByUserIdAndUsedFalse(userId);
    }

    public long countAvailable(Long userId) {
        return couponRepository.countByUserIdAndUsedFalse(userId);
    }

    @Transactional
    public void useCoupon(Long couponId, Long userId) {
        Coupon c = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("优惠券不存在"));
        if (!c.getUser().getId().equals(userId))
            throw new RuntimeException("无权使用");
        if (c.getUsed())
            throw new RuntimeException("已使用");
        c.setUsed(true);
        couponRepository.save(c);
    }
}
