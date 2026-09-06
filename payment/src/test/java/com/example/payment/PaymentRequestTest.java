package com.example.payment;

import com.example.payment.domain.ExternalAccount;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.domain.enums.PaymentState;
import com.example.payment.domain.enums.Rail;
import com.example.payment.domain.enums.RequestType;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PaymentRequestTest {

    @Test
    void newPaymentRequestStartsInInitiatedState() {
        ExternalAccount debitAccount = ExternalAccount.companyAccount("ACME", UUID.randomUUID(), Rail.BANK_TRANSFER, null, null, "admin-company");
        ExternalAccount creditAccount = ExternalAccount.individualAccount("Jane Doe", UUID.randomUUID(), Rail.BANK_TRANSFER, "iban", "Jane Doe");
        PaymentRequest request = PaymentRequest.create(UUID.randomUUID(), debitAccount, creditAccount, RequestType.PAYROLL, new BigDecimal(100), "EUR");

        assertThat(request.getPaymentState()).isEqualTo(PaymentState.INITIATED);
    }

//    @Test
//    void initiatedCanTransitionToPending() {
//
//    }

    @Test
    void settledCannotTransitionBackToPending() {}

    @Test
    void failedPaymentMovesToCompensatingBeforeReversed() {}
}
