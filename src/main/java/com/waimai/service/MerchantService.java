package com.waimai.service;

import com.waimai.entity.Merchant;
import com.waimai.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家服务 — 店铺管理、搜索
 */
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    /** 按用户 ID 查找商家 */
    public Merchant findByUserId(Long userId) {
        return merchantRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
    }

    /** 按 ID 查找商家 */
    public Merchant findById(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商家不存在"));
    }

    /** 搜索营业中的商家 */
    public List<Merchant> searchOpenMerchants(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return merchantRepository.findByStatusOrderByMonthlySalesDesc("营业中");
        }
        return merchantRepository.findByShopNameContainingAndStatus(keyword, "营业中");
    }

    /** 按销量排序 */
    public List<Merchant> listBySales() {
        return merchantRepository.findByStatusOrderByMonthlySalesDesc("营业中");
    }

    /** 按评分排序 */
    public List<Merchant> listByRating() {
        return merchantRepository.findByStatusOrderByRatingDesc("营业中");
    }

    /** 更新店铺信息 */
    @Transactional
    public void updateShopInfo(Long merchantId, String shopName, String shopAvatar,
                                String description, String businessHours,
                                BigDecimal deliveryFee, BigDecimal minOrderAmount) {
        Merchant merchant = findById(merchantId);
        merchant.setShopName(shopName);
        if (shopAvatar != null && !shopAvatar.isBlank()) {
            merchant.setShopAvatar(shopAvatar);
        }
        merchant.setDescription(description);
        merchant.setBusinessHours(businessHours);
        merchant.setDeliveryFee(deliveryFee);
        merchant.setMinOrderAmount(minOrderAmount);
        merchantRepository.save(merchant);
    }

    /** 切换营业状态 */
    @Transactional
    public void toggleStatus(Long merchantId) {
        Merchant merchant = findById(merchantId);
        if ("营业中".equals(merchant.getStatus())) {
            merchant.setStatus("休息中");
        } else {
            merchant.setStatus("营业中");
        }
        merchantRepository.save(merchant);
    }
}
