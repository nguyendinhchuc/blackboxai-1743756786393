package com.ecommerce.api.controller.admin;

import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        // Add counts to the model
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("activeBanners", bannerService.countActiveBanners());
        model.addAttribute("totalUsers", userRepository.count());

        // Add revenue statistics
        Map<String, Object> revenueStats = statisticsService.getRevenueStatistics(null, null);
        model.addAttribute("totalRevenue", revenueStats.getOrDefault("totalRevenue", 0));
        model.addAttribute("totalTransactions", revenueStats.getOrDefault("totalTransactions", 0));
        model.addAttribute("averageTransactionValue", revenueStats.getOrDefault("averageTransactionValue", 0));

        // Add inventory statistics
        Map<String, Object> inventoryStats = statisticsService.getInventoryStatistics();
        model.addAttribute("lowStockProducts", inventoryStats.getOrDefault("lowStockProducts", 0));
        model.addAttribute("outOfStockProducts", inventoryStats.getOrDefault("outOfStockProducts", 0));
        model.addAttribute("totalStock", inventoryStats.getOrDefault("totalStock", 0));
        model.addAttribute("totalInventoryValue", inventoryStats.getOrDefault("totalInventoryValue", 0));

        // Add any recent activities (this could be enhanced with a dedicated activity service)
        // model.addAttribute("recentActivities", activityService.getRecentActivities());

        return "admin/dashboard/index";
    }

    @GetMapping("/dashboard")
    public String redirectToDashboard() {
        return "redirect:/admin";
    }
}
