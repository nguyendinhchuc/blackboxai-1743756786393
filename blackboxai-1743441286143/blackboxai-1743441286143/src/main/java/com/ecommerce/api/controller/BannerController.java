package com.ecommerce.api.controller;

import com.ecommerce.api.model.Banner;
import com.ecommerce.api.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }

    @PostMapping
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        return ResponseEntity.ok(bannerService.createBanner(banner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> updateBanner(
            @PathVariable Long id,
            @RequestBody Banner banner) {
        return ResponseEntity.ok(bannerService.updateBanner(id, banner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/order/{order}")
    public ResponseEntity<Banner> updateBannerOrder(
            @PathVariable Long id,
            @PathVariable Integer order) {
        return ResponseEntity.ok(bannerService.updateBannerOrder(id, order));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<Banner> toggleBannerActive(@PathVariable Long id) {
        return ResponseEntity.ok(bannerService.toggleBannerActive(id));
    }
}