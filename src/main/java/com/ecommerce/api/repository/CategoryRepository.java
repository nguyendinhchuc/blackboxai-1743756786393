package com.ecommerce.api.repository;

import com.ecommerce.api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentCategoryIsNullAndTenantId(Long tenantId);
    List<Category> findByParentCategoryIdAndTenantId(Long parentId, Long tenantId);
    List<Category> findByTenantId(Long tenantId);
}