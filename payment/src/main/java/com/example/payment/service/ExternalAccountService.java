package com.example.payment.service;

import com.example.payment.domain.ExternalAccount;
import com.example.payment.repository.ExternalAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExternalAccountService {

    @Autowired
    private ExternalAccountRepository externalAccountRepository;

    public ExternalAccount saveAccount(ExternalAccount externalAccount) {
        UUID walletRef = externalAccount.getWalletReference();
        if (walletRef == null) {
            throw new IllegalArgumentException("Create ledger wallet and proceed onboarding process before creating account");
        }

        String companyOnboardingId = externalAccount.getSwanAccountId();
        String companyOnboardingIBAN = externalAccount.getSwanIban();
        String individualIBAN = externalAccount.getIban();

        if (companyOnboardingId == null && companyOnboardingIBAN == null && individualIBAN == null) {
            throw new IllegalArgumentException("Create ledger wallet and proceed onboarding process before creating account");
        }

        return externalAccountRepository.save(externalAccount);
    }

    public ExternalAccount getAccount(UUID id) {
        Optional<ExternalAccount> account = externalAccountRepository.findById(id);
        return account.orElse(null);
    }

    // **beneficiaries** - require SCA/consent for SWAN

    // **company** - create new account in SWAN

}
