package com.ecommerce.api.controller.admin;

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

        // Add any recent activities (this could be enhanced with a dedicated activity service)
        // model.addAttribute("recentActivities", activityService.getRecentActivities());

        return "admin/dashboard/index";
    }

    @GetMapping("/dashboard")
    public String redirectToDashboard() {
        return "redirect:/admin";
    }
}
