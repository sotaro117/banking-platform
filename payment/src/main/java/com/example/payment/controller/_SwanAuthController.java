package com.example.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/auth")
class _SwanAuthController {
    @GetMapping
    public String callback(@RequestParam String code, @RequestParam String state) {

        System.out.println("Authorization code: " + code);

        System.out.println("State: " + state);

        return "OAuth successful";
    }
}
