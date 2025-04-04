package com.ecommerce.api.repository;

import com.ecommerce.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndTenantId(String username, Long tenantId);
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    Boolean existsByUsernameAndTenantId(String username, Long tenantId);
    Boolean existsByEmailAndTenantId(String email, Long tenantId);
    List<User> findByTenantId(Long tenantId);
}