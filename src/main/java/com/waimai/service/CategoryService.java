package com.waimai.service;

import com.waimai.entity.Category;
import com.waimai.entity.Merchant;
import com.waimai.repository.CategoryRepository;
import com.waimai.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MerchantRepository merchantRepository;

    /** 按商家查找所有分类 */
    public List<Category> listByMerchant(Long merchantId) {
        return categoryRepository.findByMerchantIdOrderBySortOrderAsc(merchantId);
    }

    /** 添加分类 — 使用 getReferenceById 获取 JPA 代理对象，避免 builder().id() 游离代理问题 */
    @Transactional
    public Category add(Long merchantId, String name, Integer sortOrder) {
        Category category = Category.builder()
                .merchant(merchantRepository.getReferenceById(merchantId))
                .name(name)
                .sortOrder(sortOrder)
                .build();
        return categoryRepository.save(category);
    }

    /** 编辑分类 */
    @Transactional
    public void update(Long categoryId, String name, Integer sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        category.setName(name);
        category.setSortOrder(sortOrder);
        categoryRepository.save(category);
    }

    /** 删除分类 */
    @Transactional
    public void delete(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
