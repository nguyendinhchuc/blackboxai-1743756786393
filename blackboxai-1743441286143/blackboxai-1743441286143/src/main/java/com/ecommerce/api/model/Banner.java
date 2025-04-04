package com.ecommerce.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Data
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String imageUrl;

    private String linkUrl;

    private Integer displayOrder;

    private Boolean isActive;

    @Column(name = "tenant_id")
    private Long tenantId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }
}