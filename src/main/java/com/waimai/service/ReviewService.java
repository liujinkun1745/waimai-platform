package com.waimai.service;

import com.waimai.entity.*;
import com.waimai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    /** 提交评价 */
    @Transactional
    public Review submit(Long orderId, Long consumerId, int taste, int packaging, int delivery, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!order.getConsumer().getId().equals(consumerId))
            throw new RuntimeException("无权操作");
        if (!"已完成".equals(order.getStatus()))
            throw new RuntimeException("只能评价已完成的订单");
        if (reviewRepository.findByOrderId(orderId).isPresent())
            throw new RuntimeException("该订单已评价过");

        Review review = Review.builder()
                .order(order)
                .merchant(order.getMerchant())
                .consumer(userRepository.getReferenceById(consumerId))
                .tasteRating(taste)
                .packagingRating(packaging)
                .deliveryRating(delivery)
                .comment(comment != null ? comment : "")
                .build();
        review = reviewRepository.save(review);

        // 更新商家评分
        double avg = reviewRepository.getAverageRating(order.getMerchant().getId());
        Merchant m = order.getMerchant();
        m.setRating(BigDecimal.valueOf(Math.round(avg * 10) / 10.0));
        merchantRepository.save(m);

        return review;
    }

    /** 商家所有评价 */
    public List<Review> listByMerchant(Long merchantId) {
        return reviewRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId);
    }

    /** 评价数 */
    public long countByMerchant(Long merchantId) {
        return reviewRepository.countByMerchantId(merchantId);
    }

    /** 订单是否已评价 */
    public boolean existsByOrder(Long orderId) {
        return reviewRepository.findByOrderId(orderId).isPresent();
    }
}
