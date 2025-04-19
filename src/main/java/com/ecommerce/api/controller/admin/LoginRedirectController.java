package com.ecommerce.api.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginRedirectController {

    @GetMapping("/login")
    public String redirectToCustomLogin() {
        return "redirect:/admin/login";
    }
}
