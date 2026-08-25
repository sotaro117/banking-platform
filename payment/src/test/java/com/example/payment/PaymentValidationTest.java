package com.example.payment;

import com.example.payment.repository.PaymentRequestRepository;
import com.example.payment.service.PaymentRequestService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class PaymentValidationTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @InjectMocks
    private PaymentRequestService paymentRequestService;

    @Test
    void rejectsPaymentAgainstRevenueAccount() {
        String wallet = """
                {
                    \"id\":\"wallet123\", 
                    \"accountType\":\"EXPENSE\", 
                    \"party\": \"party\", 
                    \"entries\": \"entries\", 
                    \"currency\": \"EUR\", 
                    \"status\": \"ACTIVE\", 
                    \"createdAt\": \"null\"
                }
                """;
        assertThatThrownBy(() -> paymentRequestService.validateWallet(wallet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cannot use EXPENSE type wallet for payment request");
    }

    @Test
    void acceptsPaymentAgainstLiabilityAccount() {
        String wallet = """
                {
                    \"id\":\"wallet123\", 
                    \"accountType\":\"LIABILITY\", 
                    \"party\": \"party\", 
                    \"entries\": \"entries\", 
                    \"currency\": \"EUR\", 
                    \"status\": \"ACTIVE\", 
                    \"createdAt\": \"null\"
                }
                """;

        assertDoesNotThrow(() -> paymentRequestService.validateWallet(wallet));
    }
}
