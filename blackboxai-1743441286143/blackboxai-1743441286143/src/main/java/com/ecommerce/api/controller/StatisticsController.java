package com.ecommerce.api.controller;

import com.ecommerce.api.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
}