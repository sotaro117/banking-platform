package com.example.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class SwanWebhookController {
    @PostMapping
    public void handlePaymentProcess(@RequestBody String body) {
        System.out.println("Incoming request: " + body);
    }
}
