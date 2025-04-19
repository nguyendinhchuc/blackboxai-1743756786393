package com.ecommerce.api.repository;

import com.ecommerce.api.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategoryId(Long parentId);
    List<Category> findByParentCategoryIsNullAndTenantId(Long tenantId);
    List<Category> findByParentCategoryIdAndTenantId(Long parentId, Long tenantId);
    List<Category> findByTenantId(Long tenantId);
    Page<Category> findByTenantId(Long tenantId, Pageable pageable);
    Page<Category> findByNameContainingIgnoreCaseAndTenantId(String name, Long tenantId, Pageable pageable);
    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);
}
