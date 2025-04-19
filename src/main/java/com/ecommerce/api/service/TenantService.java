package com.ecommerce.api.service;

import com.ecommerce.api.model.Tenant;
import com.ecommerce.api.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {
    @Autowired
    private TenantRepository tenantRepository;

    public Tenant createTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    /**
     * @deprecated Use {@link #getAllTenants(Pageable)} instead for pagination support.
     */
    @Deprecated
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Page<Tenant> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }

    public Tenant getTenantByDomain(String domain) {
        return tenantRepository.findByDomain(domain)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }
}
