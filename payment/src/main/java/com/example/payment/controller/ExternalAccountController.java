package com.example.payment.controller;

import com.example.payment.connector.SwanAdapter;
import com.example.payment.domain.ExternalAccount;
import com.example.payment.domain.swan.onboarding.create.CompanyOnboarding;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.service.ExternalAccountService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/external-account")
public class ExternalAccountController {
    @Autowired
    ExternalAccountService externalAccountService;
    @Autowired
    SwanAdapter swanAdapter;

    @PostMapping("/create")
    ResponseEntity<Void> createExternalAccount(@RequestBody ExternalAccount account) {
        externalAccountService.saveAccount(account);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    ResponseEntity<ExternalAccount> getExternalAccount(@PathVariable UUID id) {
        ExternalAccount account = externalAccountService.getAccount(id);

        return ResponseEntity.ok().body(account);
    }

    // onboarding with swan
    @PostMapping("/onboarding/create")
    ResponseEntity<Void> startOnboardingProcess(@RequestBody CompanyOnboarding companyOnboarding) {
        swanAdapter.createCompanyOnboarding(companyOnboarding);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/onboarding/get")
    ResponseEntity<AccountHolderOnboardingsResponse> getOnboarding() {
        AccountHolderOnboardingsResponse onboardings = swanAdapter.getCompanyOnboarding();

        return ResponseEntity.ok().body(onboardings);
    }

    @PostMapping("/onbboarding/update/{id}")
    ResponseEntity<Void> updateOnboardingData(@PathVariable String id, @RequestBody CompanyOnboarding companyOnboarding) {
        swanAdapter.updateCompanyOnboarding(id, companyOnboarding);

        return ResponseEntity.ok().build();
    }

    // later
//    @PostMapping("/onboarding/document")
//    ResponseEntity<Void> submitDocument() {
//        swanAdapter.uploadOnboardingDocument();
//    }
//
//    @PostMapping("/onboarding/review")
//    ResponseEntity<Void> reviewOnboardingSubmission() {}
}
