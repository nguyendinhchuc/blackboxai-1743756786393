package com.ecommerce.api.service;

import com.ecommerce.api.interceptor.TenantContext;
import com.ecommerce.api.model.Banner;
import com.ecommerce.api.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerService {

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
}