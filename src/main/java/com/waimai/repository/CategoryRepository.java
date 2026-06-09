package com.waimai.repository;

import com.waimai.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 商品分类数据访问层
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** 按商家 ID 查找所有分类，按排序字段排序 */
    List<Category> findByMerchantIdOrderBySortOrderAsc(Long merchantId);
}
