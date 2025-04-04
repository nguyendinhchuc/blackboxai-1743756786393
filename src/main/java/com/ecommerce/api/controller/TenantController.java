package com.ecommerce.api.controller;

import com.ecommerce.api.model.Tenant;
import com.ecommerce.api.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    @Autowired
    private TenantService tenantService;

    @PostMapping
    public Tenant createTenant(@RequestBody Tenant tenant) {
        return tenantService.createTenant(tenant);
    }

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/domain/{domain}")
    public Tenant getTenantByDomain(@PathVariable String domain) {
        return tenantService.getTenantByDomain(domain);
    }
}