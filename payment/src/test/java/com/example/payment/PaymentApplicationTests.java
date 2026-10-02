package com.example.payment;

import com.example.payment.connector.SwanAdapter;
import com.example.payment.domain.ExternalAccount;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.domain.enums.PaymentState;
import com.example.payment.domain.enums.Rail;
import com.example.payment.domain.enums.RequestType;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.domain.swan.onboarding.retrieve.OnboardingEdge;
import com.example.payment.repository.ExternalAccountRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentApplicationTests {
    @LocalServerPort
    private int port;

    private RestClient restClient = RestClient.builder()
            .build();
    @Autowired
    private ExternalAccountRepository externalAccountRepository;
    @Autowired
    private SwanAdapter swanAdapter;

    // create wallet > proceed onboarding > create external account
    @Test
    void shouldCreateExternalAccount() {
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge onboarding = onboardingList.edges().get(1);

        String holderName = onboarding.node().company().name();
        String swanAccountId = onboarding.node().account().id();
        String swanIban = onboarding.node().account().IBAN();

        // before creating external account the app is supposed to require wallet creation and finalized onboarding

        ExternalAccount externalAccount = ExternalAccount.companyAccount(holderName, UUID.randomUUID(), Rail.BANK_TRANSFER, swanAccountId, swanIban, "company account");
        ResponseEntity<Void> response = restClient
                .post()
                .uri("http://localhost:{port}/external-account/create", port)
                .body(externalAccount)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

//        ResponseEntity<ExternalAccount> getResponse = restClient
//                .get()
//                .uri("http://localhost:{port}/external-account/", port)
//                .retrieve()
//                .toEntity(ExternalAccount.class);
//
//        DocumentContext documentContext = JsonPath.parse(getResponse);
//
//        String savedSwanAccountId = documentContext.read("$.swanAccountId");
//        String savedSwanIban = documentContext.read("$.swanIban");
//
//        assertThat(savedSwanAccountId).isEqualTo(swanAccountId);
//        assertThat(savedSwanIban).isEqualTo(swanIban);
    }

    @Test
    void includeIdempotencyKeyInHeader() {
        String idempotencyKey = UUID.randomUUID().toString();
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge recentOnboarding = onboardingList.edges().get(1);
        ExternalAccount debitAccount = ExternalAccount.companyAccount("ACME", UUID.fromString("3832138c-21c9-4d5f-adb6-9bad9b4772d9"), Rail.BANK_TRANSFER, recentOnboarding.node().account().id(), recentOnboarding.node().account().IBAN(), "admin-company");
        ExternalAccount creditAccount = ExternalAccount.individualAccount("Sotaro Takahata", UUID.fromString("709b2582-e4df-4805-a66f-72ec2d3e9856"), Rail.BANK_TRANSFER, "ES6111112222026279844350", "My Personal Account");

        externalAccountRepository.saveAll(List.of(debitAccount, creditAccount));
        UUID requestId = UUID.randomUUID();
        PaymentRequest request = PaymentRequest.create(requestId, debitAccount, creditAccount, RequestType.PAYROLL, new BigDecimal(100), "EUR");

        ResponseEntity<Void> response = restClient
                .post()
                .uri("http://localhost:{port}/payment", port)
                .header("Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> getResponse = restClient
                .get()
                .uri("http://localhost:{port}/payment/{id}", port, requestId)
                .retrieve()
                .toEntity(String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        String savedIdepomtencyKey = documentContext.read("$.idempotencyKey");

        assertThat(savedIdepomtencyKey).isEqualTo(idempotencyKey);
    }

    // must test with real db
    @Test
    void shouldCompensateAndReverseIfTransactionFailed() {
        String idempotencyKey = UUID.randomUUID().toString();
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge recentOnboarding = onboardingList.edges().get(1);
        ExternalAccount debitAccount = ExternalAccount.companyAccount("ACME", UUID.fromString("3832138c-21c9-4d5f-adb6-9bad9b4772d9"), Rail.BANK_TRANSFER, recentOnboarding.node().account().id(), recentOnboarding.node().account().IBAN(), "admin-company");
        ExternalAccount creditAccount = ExternalAccount.individualAccount("Sotaro Takahata", UUID.fromString("709b2582-e4df-4805-a66f-72ec2d3e9856"), Rail.BANK_TRANSFER, "ES6111112222026279844350", "My Personal Account");

        externalAccountRepository.saveAll(List.of(debitAccount, creditAccount));
        UUID requestId = UUID.randomUUID();
        // account balance: 100 euro -> should reject
        PaymentRequest request = PaymentRequest.create(requestId, debitAccount, creditAccount, RequestType.PAYROLL, new BigDecimal(200), "EUR");

        ResponseEntity<Void> response = restClient
                .post()
                .uri("http://localhost:{port}/payment", port)
                .header("Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .toBodilessEntity();

        // wait for webhook
        Awaitility.await()
            .atLeast(Duration.ofSeconds(10))
            .atMost(Duration.ofSeconds(30));

        // assert payment request if complete
        ResponseEntity<String> getResponse = restClient
                .get()
                .uri("http://localhost:{port}/payment/{id}", port, requestId)
                .retrieve()
                .toEntity(String.class);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        String paymentStateString = documentContext.read("$.paymentState");
        PaymentState paymentState = PaymentState.valueOf(paymentStateString);
        assertThat(paymentState).isEqualTo(PaymentState.REVERSED);

        // assert if reversed balance (ledger)
        ResponseEntity<String> walletBalance = restClient
                .get()
                .uri("http://localhost:{port}/wallet/{id}/balance", port, debitAccount.getWalletReference())
                .retrieve()
                .toEntity(String.class);

        BigDecimal balance = new BigDecimal(walletBalance.getBody());
        assertThat(balance).isGreaterThan(new BigDecimal(200));
    }
}
