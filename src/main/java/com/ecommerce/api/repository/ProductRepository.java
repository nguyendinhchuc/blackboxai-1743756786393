package com.ecommerce.api.repository;

import com.ecommerce.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);
    List<Product> findByTenantId(Long tenantId);
}