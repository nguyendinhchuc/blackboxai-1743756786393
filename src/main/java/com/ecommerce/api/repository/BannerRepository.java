package com.ecommerce.api.repository;

import com.ecommerce.api.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByTenantIdOrderByDisplayOrderAsc(Long tenantId);

    List<Banner> findByTenantIdAndIsActiveTrueAndStartDateBeforeAndEndDateAfterOrderByDisplayOrderAsc(
            Long tenantId,
            LocalDateTime now,
            LocalDateTime now2
    );

    List<Banner> findByTenantIdAndIsActiveTrue(Long tenantId);

    /**
     * Find banners by tenant ID ordered by display order in descending order
     * @param tenantId the tenant ID
     * @return list of banners
     */
    List<Banner> findByTenantIdOrderByDisplayOrderDesc(Long tenantId);
}
