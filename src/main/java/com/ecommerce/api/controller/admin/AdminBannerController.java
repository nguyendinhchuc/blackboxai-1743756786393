package com.ecommerce.api.controller.admin;

import com.ecommerce.api.model.Banner;
import com.ecommerce.api.service.BannerService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public String listBanners(Model model) {
        // Get banners sorted by display order
        model.addAttribute("banners", bannerService.getActiveBanners());
        return "admin/banners/list";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Banner> getBanner(@PathVariable Long id) {
        try {
            Banner banner = bannerService.getBannerById(id);
            return ResponseEntity.ok(banner);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public String createBanner(
            @ModelAttribute Banner banner,
            @RequestParam("image") MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            // Set display order if not provided
            if (banner.getDisplayOrder() == null) {
                banner.setDisplayOrder(bannerService.getNextDisplayOrder());
            }

            // Handle image upload
            if (!image.isEmpty()) {
                String imageUrl = bannerService.uploadImage(image);
                banner.setImageUrl(imageUrl);
            }

            bannerService.save(banner);
            redirectAttributes.addFlashAttribute("success", "Banner created successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create banner: " + e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @PostMapping("/{id}/update")
    public String updateBanner(
            @PathVariable Long id,
            @ModelAttribute Banner banner,
            @RequestParam(value = "image", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            Banner existingBanner = bannerService.getBannerById(id);
            banner.setId(id);
            
            // Keep existing image if no new image is uploaded
            if (image == null || image.isEmpty()) {
                banner.setImageUrl(existingBanner.getImageUrl());
            } else {
                // Upload new image and update URL
                String imageUrl = bannerService.uploadImage(image);
                banner.setImageUrl(imageUrl);
                
                // Delete old image
                bannerService.deleteImage(existingBanner.getImageUrl());
            }

            bannerService.save(banner);
            redirectAttributes.addFlashAttribute("success", "Banner updated successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update banner: " + e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteBanner(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            Banner banner = bannerService.getBannerById(id);
            
            // Delete banner image
            if (banner.getImageUrl() != null) {
                bannerService.deleteImage(banner.getImageUrl());
            }
            
            bannerService.deleteBanner(id);
            redirectAttributes.addFlashAttribute("success", "Banner deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete banner: " + e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<?> reorderBanners(@RequestBody Long[] bannerIds) {
        try {
            List<Long> bannerIdList = Arrays.stream(bannerIds).collect(Collectors.toList());
            bannerService.reorderBanners(bannerIdList);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reorder banners: " + e.getMessage());
        }
    }
}
