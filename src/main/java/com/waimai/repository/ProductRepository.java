package com.waimai.repository;

import com.waimai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * 商品数据访问层
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 按商家和分类查找上架商品 */
    List<Product> findByMerchantIdAndCategoryIdAndStatus(Long merchantId, Long categoryId, String status);

    /** 按商家查找所有上架商品 */
    List<Product> findByMerchantIdAndStatus(Long merchantId, String status);

    /** 按商家查找所有商品（含下架） */
    List<Product> findByMerchantId(Long merchantId);

    /** 按商家查找所有商品（含下架），JOIN FETCH 分类避免 N+1 */
    @Query("SELECT DISTINCT p FROM Product p JOIN FETCH p.category WHERE p.merchant.id = :merchantId")
    List<Product> findByMerchantIdWithCategory(Long merchantId);

    /** 按名称模糊搜索上架商品 */
    List<Product> findByMerchantIdAndNameContainingAndStatus(Long merchantId, String name, String status);
}
