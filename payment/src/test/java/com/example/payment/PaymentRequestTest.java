package com.example.payment;

import com.example.payment.domain.ExternalAccount;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.domain.enums.PaymentState;
import com.example.payment.domain.enums.Rail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class PaymentRequestTest {

    @Test
    void newPaymentRequestStartsInInitiatedState() {
        ExternalAccount exAccount = new ExternalAccount(UUID.randomUUID(), UUID.randomUUID(), Rail.BANK_TRANSFER, "example-stripe-id", "company-payout");
        PaymentRequest request = PaymentRequest.create(UUID.randomUUID(), exAccount, new BigDecimal(100), "EUR");

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
