package com.ecommerce.api.repository;

import com.ecommerce.api.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct_TenantId(Long tenantId);
    List<ProductImage> findByProductIdAndProduct_TenantId(Long productId, Long tenantId);
}