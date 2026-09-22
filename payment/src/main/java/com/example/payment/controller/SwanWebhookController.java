package com.example.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class SwanWebhookController {
    @PostMapping("/payment")
    public void handlePaymentProcess(@RequestBody Map<String, Object> payload) {
        System.out.println("Incoming request: " + payload);
    }

    @PostMapping("/consent")
    public void handleCriticalProcess(@RequestBody Map<String, Object> payload) {
        System.out.println("Incoming request: " + payload);
    }
}
