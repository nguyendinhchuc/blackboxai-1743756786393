package com.ecommerce.api.controller.admin;

import com.ecommerce.api.model.Product;
import com.ecommerce.api.model.ProductImage;
import com.ecommerce.api.service.CategoryService;
import com.ecommerce.api.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage;
        
        if (search != null && !search.isEmpty()) {
            productPage = productService.searchProducts(search, pageRequest);
        } else if (categoryId != null) {
            productPage = productService.findByCategory(categoryId, pageRequest);
        } else {
            productPage = productService.findAll(pageRequest);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("categories", categoryService.findAll(PageRequest.of(0, 100)).getContent());

        return "admin/products/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll(PageRequest.of(0, 100)).getContent());
        return "admin/products/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll(PageRequest.of(0, 100)).getContent());
        return "admin/products/form";
    }

    @PostMapping("/create")
    public String createProduct(
            @ModelAttribute Product product,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("images") MultipartFile[] images,
            RedirectAttributes redirectAttributes) {
        
        try {
            product.setCategory(categoryService.findById(categoryId));
            Product savedProduct = productService.save(product);
            
            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        productService.addProductImage(savedProduct, image);
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Product created successfully");
            return "redirect:/admin/products";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload images: " + e.getMessage());
            return "redirect:/admin/products/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product: " + e.getMessage());
            return "redirect:/admin/products/new";
        }
    }

    @PostMapping("/{id}/update")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product product,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            RedirectAttributes redirectAttributes) {
        
        try {
            Product existingProduct = productService.findById(id);
            product.setId(id);
            product.setCategory(categoryService.findById(categoryId));
            product.setImages(existingProduct.getImages()); // Preserve existing images
            
            Product updatedProduct = productService.save(product);
            
            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        productService.addProductImage(updatedProduct, image);
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Product updated successfully");
            return "redirect:/admin/products";
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload images: " + e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update product: " + e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }

    @DeleteMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @DeleteMapping("/images/{imageId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        try {
            productService.deleteProductImage(imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete image: " + e.getMessage());
        }
    }
}
