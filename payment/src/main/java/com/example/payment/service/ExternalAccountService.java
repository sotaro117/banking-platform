package com.example.payment.service;

import com.example.payment.domain.ExternalAccount;
import com.example.payment.repository.ExternalAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalAccountService {

    @Autowired
    private ExternalAccountRepository externalAccountRepository;

    public ExternalAccount saveAccount(ExternalAccount externalAccount) {
        return externalAccountRepository.save(externalAccount);
    }

    // **beneficiaries** - require SCA/consent for SWAN

    // **company** - create new account in SWAN

}
