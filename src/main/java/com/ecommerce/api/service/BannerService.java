package com.ecommerce.api.service;

import com.ecommerce.api.interceptor.TenantContext;
import com.ecommerce.api.model.Banner;
import com.ecommerce.api.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BannerService {

    @Value("${app.upload.dir:uploads/banners}")
    private String uploadDir;

    @Autowired
    private BannerRepository bannerRepository;

    public List<Banner> getAllBanners() {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        return bannerRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId);
    }

    public List<Banner> getActiveBanners() {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        LocalDateTime now = LocalDateTime.now();
        return bannerRepository.findByTenantIdAndIsActiveTrueAndStartDateBeforeAndEndDateAfterOrderByDisplayOrderAsc(
                tenantId, now, now);
    }

    public Banner getBannerById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));
    }

    @Transactional
    public Banner createBanner(Banner banner) {
        banner.setTenantId(TenantContext.getCurrentTenant().getId());
        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner updateBanner(Long id, Banner bannerDetails) {
        Banner banner = getBannerById(id);

        banner.setTitle(bannerDetails.getTitle());
        banner.setDescription(bannerDetails.getDescription());
        banner.setImageUrl(bannerDetails.getImageUrl());
        banner.setLinkUrl(bannerDetails.getLinkUrl());
        banner.setDisplayOrder(bannerDetails.getDisplayOrder());
        banner.setIsActive(bannerDetails.getIsActive());
        banner.setStartDate(bannerDetails.getStartDate());
        banner.setEndDate(bannerDetails.getEndDate());

        return bannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = getBannerById(id);
        // Delete the banner image if it exists
        if (banner.getImageUrl() != null) {
            deleteImage(banner.getImageUrl());
        }
        bannerRepository.delete(banner);
    }

    @Transactional
    public Banner updateBannerOrder(Long id, Integer order) {
        Banner banner = getBannerById(id);
        banner.setDisplayOrder(order);
        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner toggleBannerActive(Long id) {
        Banner banner = getBannerById(id);
        banner.setIsActive(!banner.getIsActive());
        return bannerRepository.save(banner);
    }

    public long countActiveBanners() {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        LocalDateTime now = LocalDateTime.now();
        return bannerRepository.findByTenantIdAndIsActiveTrueAndStartDateBeforeAndEndDateAfterOrderByDisplayOrderAsc(
                tenantId, now, now).size();
    }

    /**
     * Get the next available display order number
     * @return next display order number
     */
    public Integer getNextDisplayOrder() {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        return bannerRepository.findByTenantIdOrderByDisplayOrderDesc(tenantId)
                .stream()
                .findFirst()
                .map(banner -> banner.getDisplayOrder() + 1)
                .orElse(1);
    }

    /**
     * Upload banner image and return the URL
     * @param image MultipartFile to upload
     * @return URL of the uploaded image
     * @throws IOException if file handling fails
     */
    public String uploadImage(MultipartFile image) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        // Save the file
        Files.copy(image.getInputStream(), filePath);

        // Return the relative URL
        return "/uploads/banners/" + filename;
    }

    /**
     * Delete banner image file
     * @param imageUrl URL of the image to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteImage(String imageUrl) {
        try {
            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, filename);

            // Delete file if it exists
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + imageUrl, e);
        }
    }

    /**
     * Save banner entity
     * @param banner entity to save
     * @return saved banner
     */
    @Transactional
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            // New banner
            banner.setTenantId(TenantContext.getCurrentTenant().getId());
            if (banner.getDisplayOrder() == null) {
                banner.setDisplayOrder(getNextDisplayOrder());
            }
        }
        return bannerRepository.save(banner);
    }

    /**
     * Reorder banners based on the provided list of banner IDs
     * The order in the list determines the new display order
     * @param bannerIds list of banner IDs in desired order
     */
    @Transactional
    public void reorderBanners(List<Long> bannerIds) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        AtomicInteger order = new AtomicInteger(1);

        bannerIds.forEach(id -> {
            Banner banner = getBannerById(id);
            // Verify banner belongs to current tenant
            if (!banner.getTenantId().equals(tenantId)) {
                throw new RuntimeException("Banner does not belong to current tenant: " + id);
            }
            banner.setDisplayOrder(order.getAndIncrement());
            bannerRepository.save(banner);
        });
    }
}
