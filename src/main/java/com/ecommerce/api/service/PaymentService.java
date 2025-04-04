package com.ecommerce.api.service;

import com.ecommerce.api.model.Payment;
import com.ecommerce.api.model.User;
import com.ecommerce.api.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(User user, BigDecimal amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(generateTransactionId());
        
        return paymentRepository.save(payment);
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis();
    }
}