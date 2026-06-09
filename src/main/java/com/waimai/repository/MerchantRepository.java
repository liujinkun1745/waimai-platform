package com.waimai.repository;

import com.waimai.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * 商家数据访问层
 */
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    /** 按用户 ID 查找商家 */
    Optional<Merchant> findByUserId(Long userId);

    /** 按店铺名称模糊搜索 */
    List<Merchant> findByShopNameContainingAndStatus(String shopName, String status);

    /** 查找所有营业中的商家，按销量排序 */
    List<Merchant> findByStatusOrderByMonthlySalesDesc(String status);

    /** 查找所有营业中的商家，按评分排序 */
    List<Merchant> findByStatusOrderByRatingDesc(String status);
}
