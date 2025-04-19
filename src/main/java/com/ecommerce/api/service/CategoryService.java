package com.ecommerce.api.service;

import com.ecommerce.api.interceptor.TenantContext;
import com.ecommerce.api.model.Category;
import com.ecommerce.api.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Page<Category> findAll(Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        return categoryRepository.findByTenantId(tenantId, pageable);
    }

    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId);
    }

    public Category save(Category category) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        category.setTenantId(tenantId);
        return categoryRepository.save(category);
    }

    public Category findById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        return categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }


    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setParentCategory(categoryDetails.getParentCategory());
        category.setImageUrl(categoryDetails.getImageUrl());

        return categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        Category category = categoryRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        categoryRepository.deleteById(id);
    }

    public long count() {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        return categoryRepository.findByTenantId(tenantId).size();
    }

    public Page<Category> searchCategories(String search, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        return categoryRepository.findByNameContainingIgnoreCaseAndTenantId(search, tenantId, pageable);
    }
}