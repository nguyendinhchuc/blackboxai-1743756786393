package com.ecommerce.api.controller.admin;

import com.ecommerce.api.model.User;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.BannerService;
import com.ecommerce.api.service.CategoryService;
import com.ecommerce.api.service.ProductService;
import com.ecommerce.api.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private UserDetailsServiceImpl userDetailsService;
    private ProductService productService;
    private CategoryService categoryService;
    private BannerService bannerService;
    private UserRepository userRepository;

    public AdminAuthController(UserDetailsServiceImpl userDetailsService,
                               ProductService productService,
                               CategoryService categoryService,
                               BannerService bannerService,
                               UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.bannerService = bannerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        // Check if user is already logged in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // Call init method to perform dashboard calculations after login
            init();
            return "redirect:/admin";
        }

        return "admin/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/admin/login?logout";
    }


    @GetMapping("/access-denied")
    public String accessDenied() {
        return "admin/access-denied";
    }

    public void init() {
        // Dashboard calculations
        long totalProducts = productService.count();
        long totalCategories = categoryService.count();
        long activeBanners = bannerService.countActiveBanners();
        long totalUsers = userRepository.count();

        // You can add logic here to store or use these values as needed
        // For example, logging or caching the values
        System.out.println("Dashboard Init - Total Products: " + totalProducts);
        System.out.println("Dashboard Init - Total Categories: " + totalCategories);
        System.out.println("Dashboard Init - Active Banners: " + activeBanners);
        System.out.println("Dashboard Init - Total Users: " + totalUsers);
    }
}
