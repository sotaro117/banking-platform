package com.example.payment;

import com.example.payment.connector.SwanAdapter;
import com.example.payment.domain.ExternalAccount;
import com.example.payment.domain.enums.Rail;
import com.example.payment.repository.ExternalAccountRepository;
import com.example.payment.service.ExternalAccountService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ExternalAccountTest {

    @Mock
    ExternalAccountRepository externalAccountRepository;

    @InjectMocks
    ExternalAccountService externalAccountService;

    @Test
    void mustProvideLedgerWalletIdAndOnboardingReferencesBeforeCreatingExternalAccount() {
        ExternalAccount externalAccount = ExternalAccount.companyAccount("ACME", null, Rail.BANK_TRANSFER, null, null, "admin-account");

        assertThatThrownBy(() -> externalAccountService.saveAccount(externalAccount))
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Create ledger wallet and proceed onboarding process before creating account");

        then(externalAccountRepository)
                .should(never())
                .save(externalAccount);
    }
}
