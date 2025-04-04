package com.ecommerce.api.controller;

import com.ecommerce.api.model.Payment;
import com.ecommerce.api.model.User;
import com.ecommerce.api.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Payment processPayment(@RequestParam BigDecimal amount,
                                @RequestParam String paymentMethod) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return paymentService.processPayment(user, amount, paymentMethod);
    }
}