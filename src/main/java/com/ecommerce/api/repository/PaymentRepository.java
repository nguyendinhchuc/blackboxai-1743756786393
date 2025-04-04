package com.ecommerce.api.repository;

import com.ecommerce.api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTenantId(Long tenantId);
    List<Payment> findByUserIdAndTenantId(Long userId, Long tenantId);
}