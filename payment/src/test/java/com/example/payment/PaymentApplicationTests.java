package com.example.payment;

import com.example.payment.domain.ExternalAccount;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.domain.enums.Rail;
import com.example.payment.domain.enums.RequestType;
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
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
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

    @Test
    void includeIdempotencyKeyInHeader() {
//        String idempotencyKey = UUID.randomUUID().toString();
//        ExternalAccount debitAccount = ExternalAccount.companyAccount("ACME", UUID.randomUUID(), Rail.BANK_TRANSFER, null, null, "admin-company");
//        ExternalAccount creditAccount = ExternalAccount.individualAccount("Jane Doe", UUID.randomUUID(), Rail.BANK_TRANSFER, "iban", "Jane Doe");
//
//        externalAccountRepository.save(debitAccount);
//        UUID requestId = UUID.randomUUID();
//        PaymentRequest request = PaymentRequest.create(requestId, debitAccount, creditAccount, RequestType.PAYROLL, new BigDecimal(100), "EUR");
//
//        ResponseEntity<Void> response = restClient
//                .post()
//                .uri("http://localhost:{port}/payment", port)
//                .header("Idempotency-Key", idempotencyKey)
//                .body(request)
//                .retrieve()
//                .toBodilessEntity();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//        ResponseEntity<String> getResponse = restClient
//                .get()
//                .uri("http://localhost:{port}/payment/{id}", port, requestId)
//                .retrieve()
//                .toEntity(String.class);
//
//        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
//        String savedIdepomtencyKey = documentContext.read("$.idempotencyKey");
//
//        assertThat(savedIdepomtencyKey).isEqualTo(idempotencyKey);
    }
}
