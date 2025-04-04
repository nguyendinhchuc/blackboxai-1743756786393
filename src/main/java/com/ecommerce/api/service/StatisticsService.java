package com.ecommerce.api.service;

import com.ecommerce.api.interceptor.TenantContext;
import com.ecommerce.api.model.Payment;
import com.ecommerce.api.model.Product;
import com.ecommerce.api.repository.PaymentRepository;
import com.ecommerce.api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    public Map<String, Object> getRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        List<Payment> payments = paymentRepository.findByTenantId(tenantId);
        
        // Filter by date if provided
        if (startDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            payments = payments.stream()
                .filter(p -> p.getPaymentDate().isAfter(start))
                .collect(Collectors.toList());
        }
        if (endDate != null) {
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            payments = payments.stream()
                .filter(p -> p.getPaymentDate().isBefore(end))
                .collect(Collectors.toList());
        }

        // Calculate statistics
        BigDecimal totalRevenue = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTransactions = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .count();

        BigDecimal averageTransactionValue = totalTransactions > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("totalTransactions", totalTransactions);
        statistics.put("averageTransactionValue", averageTransactionValue);
        
        // Add daily revenue if date range is provided
        if (startDate != null && endDate != null) {
            Map<LocalDate, BigDecimal> dailyRevenue = payments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .collect(Collectors.groupingBy(
                    p -> p.getPaymentDate().toLocalDate(),
                    Collectors.mapping(
                        Payment::getAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                    )
                ));
            statistics.put("dailyRevenue", dailyRevenue);
        }
        
        return statistics;
    }

    public Map<String, Object> getInventoryStatistics() {
        Long tenantId = TenantContext.getCurrentTenant().getId();
        List<Product> products = productRepository.findByTenantId(tenantId);

        // Calculate inventory statistics
        int totalProducts = products.size();
        int lowStockProducts = (int) products.stream()
            .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
            .count();
        int outOfStockProducts = (int) products.stream()
            .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() == 0)
            .count();
        int totalStock = products.stream()
            .mapToInt(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0)
            .sum();
        BigDecimal totalInventoryValue = products.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStockQuantity() != null ? p.getStockQuantity() : 0)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group products by stock status
        Map<String, List<Map<String, Object>>> productsByStockStatus = products.stream()
            .map(p -> {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", p.getId());
                productInfo.put("name", p.getName());
                productInfo.put("stockQuantity", p.getStockQuantity());
                productInfo.put("price", p.getPrice());
                return productInfo;
            })
            .collect(Collectors.groupingBy(p -> {
                Integer stock = (Integer) p.get("stockQuantity");
                if (stock == null || stock == 0) return "outOfStock";
                if (stock <= 10) return "lowStock";
                return "inStock";
            }));

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalProducts", totalProducts);
        statistics.put("lowStockProducts", lowStockProducts);
        statistics.put("outOfStockProducts", outOfStockProducts);
        statistics.put("totalStock", totalStock);
        statistics.put("totalInventoryValue", totalInventoryValue);
        statistics.put("productsByStockStatus", productsByStockStatus);
        
        return statistics;
    }
}