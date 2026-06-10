package com.waimai.service;

import com.waimai.entity.Category;
import com.waimai.entity.Merchant;
import com.waimai.entity.Product;
import com.waimai.repository.CategoryRepository;
import com.waimai.repository.MerchantRepository;
import com.waimai.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务 — 修复: 所有 builder().id() 改为 getReferenceById() 避免 FK 约束失败
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final CategoryRepository categoryRepository;

    /** 按商家和分类查找上架商品 */
    public List<Product> listByCategory(Long merchantId, Long categoryId) {
        return productRepository.findByMerchantIdAndCategoryIdAndStatus(merchantId, categoryId, "上架");
    }

    /** 按商家查找所有上架商品 */
    public List<Product> listOnSale(Long merchantId) {
        return productRepository.findByMerchantIdAndStatus(merchantId, "上架");
    }

    /** 按商家查找所有商品（含下架，供商家管理用），已 JOIN FETCH 分类 */
    public List<Product> listAll(Long merchantId) {
        return productRepository.findByMerchantIdWithCategory(merchantId);
    }

    /** 按 ID 查找 */
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    /** 添加商品 — 修复: 使用 getReferenceById 获取 JPA 代理 */
    @Transactional
    public Product add(Long merchantId, Long categoryId, String name,
                        BigDecimal price, Integer stock, String image, String description) {
        Product product = Product.builder()
                .merchant(merchantRepository.getReferenceById(merchantId))
                .category(categoryRepository.getReferenceById(categoryId))
                .name(name)
                .price(price)
                .stock(stock)
                .image(image)
                .description(description)
                .status("上架")
                .build();
        return productRepository.save(product);
    }

    /** 编辑商品 — 修复: 使用 getReferenceById 获取 JPA 代理 */
    @Transactional
    public void update(Long productId, Long categoryId, String name,
                        BigDecimal price, Integer stock, String image, String description) {
        Product product = findById(productId);
        product.setCategory(categoryRepository.getReferenceById(categoryId));
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        if (image != null && !image.isBlank()) {
            product.setImage(image);
        }
        product.setDescription(description);
        productRepository.save(product);
    }

    /** 切换上下架 */
    @Transactional
    public void toggleStatus(Long productId) {
        Product product = findById(productId);
        product.setStatus("上架".equals(product.getStatus()) ? "下架" : "上架");
        productRepository.save(product);
    }

    /** 删除商品 */
    @Transactional
    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }
}
