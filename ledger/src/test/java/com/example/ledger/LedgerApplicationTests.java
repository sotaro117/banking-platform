package com.example.ledger;

import com.example.ledger.domain.*;
import com.example.ledger.domain.enums.*;
import com.example.ledger.pendingEvent.PendingEvent;
import com.example.ledger.repository.LedgerEntryRepository;
import com.example.ledger.repository.PartyRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.repository.WalletRepository;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LedgerApplicationTests {

    @LocalServerPort
    private int port;

    private RestClient restClient = RestClient.builder()
//            .baseUrl("http://localhost:8080")
            .build();

    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void createNewPartyWithActiveStatus() {
        String displayName = "Jane Doe";
        UUID id = UUID.randomUUID();
        Party party = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);
        ResponseEntity<Void> response = restClient
                .post()
                .uri("http://localhost:{port}/party/create", port)
                .body(party)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> getResponse = restClient
            .get()
            .uri("http://localhost:{port}/party/{id}", port, id)
            .retrieve()
            .toEntity(String.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        String savedDisplayName = documentContext.read("$.displayName");
        String savedStatus = documentContext.read("$.status");

        assertThat(savedDisplayName).isEqualTo(displayName);
        assertThat(savedStatus).isEqualTo(PartyStatus.ACTIVE.name());
    }

    @Test
    void cannotCreateExistingParty() {
        String displayName = "Jane Doe";
        UUID id = UUID.randomUUID();
        Party partyA = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);
        ResponseEntity<Void> responseA = restClient
                .post()
                .uri("http://localhost:{port}/party/create", port)
                .body(partyA)
                .retrieve()
                .toBodilessEntity();

        Party partyB = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);
        ResponseEntity<Void> responseB = restClient
                .post()
                .uri("http://localhost:{port}/party/create", port)
                .body(partyB)
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .toBodilessEntity();
    }

    @Test
    void openWalletAfterCreatingAParty() {

        String displayName = "Jane Doe";
        UUID id = UUID.randomUUID();
        Party party = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);

        ResponseEntity<Void> partyResponse = restClient
                .post()
                .uri("http://localhost:{port}/party/create", port)
                .body(party)
                .retrieve()
                .toBodilessEntity();

        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.linkLiability(walletId, party, "EUR");

        ResponseEntity<Void> walletResponse = restClient
                .post()
                .uri("http://localhost:{port}/wallet/create", port)
                .body(wallet)
                .retrieve()
                .toBodilessEntity();

        ResponseEntity<String> walletGetResponse = restClient
                .get()
                .uri("http://localhost:{port}/wallet/{id}", port, walletId)
                .retrieve()
                .toEntity(String.class);

        assertThat(walletGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(walletGetResponse.getBody());

        String partyIdLinkedToWallet = documentContext.read("$['party']['id']");
        String accountType = documentContext.read("$.accountType");
        String status = documentContext.read("$.status");
        String currency = documentContext.read("$.currency");

        assertThat(UUID.fromString(partyIdLinkedToWallet)).isEqualTo(id);
        assertThat(accountType).isEqualTo(AccountType.LIABILITY.name());
        assertThat(status).isEqualTo(WalletStatus.ACTIVE.name());
        assertThat(currency).isEqualTo("EUR");
    }

    @Test
    void shouldReturnNotFoundStatusForNotExistingWallet() {
        String displayName = "Jane Doe";
        UUID id = UUID.randomUUID();
        Party party = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);

        ResponseEntity<Void> partyResponse = restClient
                .post()
                .uri("http://localhost:{port}/party/create", port)
                .body(party)
                .retrieve()
                .toBodilessEntity();

        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.linkLiability(walletId, party, "EUR");

        ResponseEntity<Void> response = restClient
                .post()
                .uri("http://localhost:{port}/wallet/create", port)
                .body(wallet)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> balanceResponse = restClient
                .get()
                .uri("http://localhost:{port}/wallet/{id}/balance", port, id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .toEntity(String.class);

        assertThat(balanceResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBalanceFromWallet() {
        String displayName = "Jane Doe";
        UUID id = UUID.randomUUID();
        Party party = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);

        ResponseEntity<Void> partyResponse = restClient
                .post()
                .uri("http://localhost:{port}/party/create", port)
                .body(party)
                .retrieve()
                .toBodilessEntity();

        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.linkLiability(walletId, party, "EUR");

        ResponseEntity<Void> response = restClient
                .post()
                .uri("http://localhost:{port}/wallet/create", port)
                .body(wallet)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> balanceResponse = restClient
                .get()
                .uri("http://localhost:{port}/wallet/{id}/balance", port, walletId)
                .retrieve()
                .toEntity(String.class);

        assertThat(balanceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        String balance = balanceResponse.getBody();

        assertThat(new BigDecimal(balance)).isEqualTo(BigDecimal.ZERO);
    }

    // phase 3
    @Test
    void getLedgerEntriesFromWalletWithinSpecifiedDate() {
//        String displayName = "Jane Doe";
//        UUID id = UUID.randomUUID();
//        Party party = new Party(id, "HR-external-registration", PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);
//
//        ResponseEntity<Void> partyResponse = restClient
//                .post()
//                .uri("http://localhost:{port}/party/create", port)
//                .body(party)
//                .retrieve()
//                .toBodilessEntity();
//
//        UUID walletId = UUID.randomUUID();
//        Wallet wallet = Wallet.linkLiability(walletId, party, "EUR");
//
//        ResponseEntity<Void> response = restClient
//                .post()
//                .uri("http://localhost:{port}/wallet/create", port)
//                .body(wallet)
//                .retrieve()
//                .toBodilessEntity();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//        ResponseEntity<String> balanceResponse = restClient
//                .get()
//                .uri("http://localhost:{port}/wallet/{id}/balance", port, walletId)
//                .retrieve()
//                .toEntity(String.class);
    }

    // phase 3
    @Test
    void shouldSavePendingEventWhenPostingTransaction() {
        String request = """
        {
            \"id\":\"f8a9c1cd-182d-44c6-944c-61cb2a9c5284\",
            \"ledgerTransactionId\":null,
            \"rail\":\"BANK_TRANSFER\",
            \"creditAccount\":\"26dac3cd-fad5-4a18-9499-d199f1baef2f\",
            \"externalAccount\":
                {\"id\":\"4e51138a-4473-4f65-ae54-bda89dfa6be2\",
                \"walletReference\":\"0955253c-9c6c-4bbb-b4b1-3baa65ff1fbb\",
                \"rail\":\"BANK_TRANSFER\",
                \"stripeFinancialAccountId\":\"example-stripe-id\",
                \"label\":\"company-payout\",
                \"createdAt\":null
                },
            \"requestType\":\"PAYROLL\",
            \"paymentState\":\"INITIATED\",
            \"idempotencyKey\":null,
            \"amount\":100,
            \"currency\":\"EUR\",
            \"stripeReference\":null,
            \"failureReason\":null,
            \"createdAt\":null
        }
        """;

        ResponseEntity<Void> response = restClient.post()
                .uri("http://localhost:{port}/internal/transaction")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
