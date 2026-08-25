package com.example.payment;

import com.example.payment.domain.IdempotencyKey;
import com.example.payment.repository.IdempotencyKeyRepository;
import com.example.payment.service.IdempotencyKeyService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;
import java.time.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class IdempotencyKeyTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @InjectMocks
    private IdempotencyKeyService idempotencyKeyService;

    @Test
    void firstRequestWithNewKeyProceeds() {
        String key = UUID.randomUUID().toString();

        given(idempotencyKeyRepository.findById(key))
                .willReturn(Optional.empty());

        assertThat(idempotencyKeyService.isExsitsIdempotencyKey(key)).isEqualTo(null);

//        then(idempotencyKeyRepository)
//                .should()
//                .save(new IdempotencyKey(key, "hash-abc", payload, Instant.now(), Instant.now().plus(Duration.ofDays(1))));
    }

    @Test
    void duplicateKeyReturnsCachedResponse() {
        String key = UUID.randomUUID().toString();
        String payload = """
                {\"id\":\"pr-1\"}
                """;

        IdempotencyKey cachedKey = new IdempotencyKey(key, "hash-abc", payload, Instant.now(), Instant.now().plus(Duration.ofDays(1)));
        given(idempotencyKeyRepository.findById(key))
                .willReturn(Optional.of(cachedKey));

        assertThat(idempotencyKeyService.isExsitsIdempotencyKey(key).getResponseSnapshot()).isEqualTo(cachedKey.getResponseSnapshot());
    }

    @Test
    void sameKeyDifferentPayloadIsRejected() {
        String key = UUID.randomUUID().toString();

        IdempotencyKey cachedKey = new IdempotencyKey(key, "hash-abc", "{\"id\":\"pr-1\"}", Instant.now(), Instant.now().plus(Duration.ofDays(1)));
        given(idempotencyKeyRepository.findById(key))
                .willReturn(Optional.of(cachedKey));

        assertThatThrownBy(() -> idempotencyKeyService.validateKey(key, "hash-diff"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
