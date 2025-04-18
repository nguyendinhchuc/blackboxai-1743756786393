package com.ecommerce.api.controller.admin;

import com.ecommerce.api.model.Category;
import com.ecommerce.api.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Category> categoryPage;
        
        if (search != null && !search.isEmpty()) {
            categoryPage = categoryService.searchCategories(search, pageRequest);
        } else {
            categoryPage = categoryService.findAll(pageRequest);
        }

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalElements", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "admin/categories/list";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Category> getCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.findById(id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public String createCategory(
            @ModelAttribute Category category,
            RedirectAttributes redirectAttributes) {
        try {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Category created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/update")
    public String updateCategory(
            @PathVariable Long id,
            @ModelAttribute Category category,
            RedirectAttributes redirectAttributes) {
        try {
            category.setId(id);
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);
            if (!category.getProducts().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete category: It has associated products. Please remove or reassign the products first.");
                return "redirect:/admin/categories";
            }
            
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete category: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
