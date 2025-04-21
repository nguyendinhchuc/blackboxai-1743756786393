package com.ecommerce.api.controller;

import com.ecommerce.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Map<String, Object> statistics = statisticsService.getRevenueStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/inventory")
    public ResponseEntity<?> getInventoryStatistics() {
        Map<String, Object> statistics = statisticsService.getInventoryStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStatistics() {
        Map<String, Object> statistics = statisticsService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserStatistics() {
        Map<String, Object> statistics = statisticsService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/order")
    public ResponseEntity<?> getOrderStatistics() {
        Map<String, Object> statistics = statisticsService.getOrderStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummaryStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("dashboard", statisticsService.getDashboardStatistics());
        summary.put("inventory", statisticsService.getInventoryStatistics());
        summary.put("revenue", statisticsService.getRevenueStatistics(startDate, endDate));
        summary.put("user", statisticsService.getUserStatistics());
        summary.put("order", statisticsService.getOrderStatistics());
        return ResponseEntity.ok(summary);
    }
}
