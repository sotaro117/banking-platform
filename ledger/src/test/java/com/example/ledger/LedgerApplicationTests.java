package com.example.ledger;

import com.example.ledger.domain.Party;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LedgerApplicationTests {

    @LocalServerPort
    private int port;

    private RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .build();

//    void openPartyAccountForEmployee() {
//        String displayName = "Jane Doe";
//        Party party = new Party(UUID.randomUUID(), externalAccount, PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);
//        ResponseEntity<Void> response = restClient
//                .post()
//                .uri("/party/create")
//                .body(party)
//                .retrieve()
//                .toBodilessEntity();
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//        String getResponse = restClient
//                .get()
//                .uri("/party?display-name={displayName}", displayName)
//                .retrieve()
//                .body(String.class);
//
//        System.out.println(getResponse);
//    }
}
