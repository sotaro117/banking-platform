package com.example.payment.service;

import com.example.payment.connector.SwanAdapter;
import com.example.payment.connector.PaymentInstruction;
import com.example.payment.connector.PayoutResult;
import com.example.payment.domain.IdempotencyKey;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.domain.enums.PaymentState;
import com.example.payment.repository.IdempotencyKeyRepository;
import com.example.payment.repository.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentRequestService {

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    @Autowired
    private IdempotencyKeyService idempotencyKeyService;
    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    private final SwanAdapter swanAdapter;

    public PaymentRequestService(SwanAdapter swanAdapter) {
        this.swanAdapter = swanAdapter;
    }

    public void iniciateRequest(PaymentRequest request) {
        RestClient restClient = RestClient.create();

        restClient.post()
                .uri("http://localhost:8081/internal/transaction")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        // change state to 'PENDING'
        request.setPaymentState(PaymentState.PENDING);
        paymentRequestRepository.save(request);
    }

//    @Cacheable(value = "request", key = "#request.idempotencyKey")
    public PaymentRequest createRequest(String idempotencyKey, PaymentRequest request) {
        IdempotencyKey existingKey = idempotencyKeyService.isExsitsIdempotencyKey(idempotencyKey);
        if (existingKey != null) {
            return paymentRequestRepository.findByIdempotencyKey(existingKey.getKey());
        }

        // validate wallet data linked as external account - company only -
        validateWallet(getWallet(request.getDebitAccount().getWalletReference()));

        try {
            String hashedTexts = request.getId() + request.getIdempotencyKey() + request.getCreatedAt();
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashbytes = digest.digest(hashedTexts.getBytes(StandardCharsets.UTF_8));
            String requestHash = bytesToHex(hashbytes);

            if (idempotencyKeyService.validateKey(idempotencyKey, requestHash)) {
                throw new RuntimeException("already exist idempotency key");
            }

            IdempotencyKey key = new IdempotencyKey(idempotencyKey, requestHash, null, Instant.now(), Instant.now().plus(Duration.ofDays(1)));

            idempotencyKeyRepository.save(key);
            request.setIdempotencyKey(idempotencyKey);

            return paymentRequestRepository.save(request);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("failed to generate hash value");
        }
    }

    public void processPayment(PaymentRequest request) {
        PayoutResult result = swanAdapter.send(PaymentInstruction.from(request));
        if (result.getStatus().equalsIgnoreCase("Rejected")) {
            request.setPaymentState(PaymentState.FAILED);
        }
    }

    public PaymentRequest getRequestById(UUID id) {
        Optional<PaymentRequest> request = paymentRequestRepository.findById(id);
        return request.orElse(null);
    }

    private String getWallet(UUID walletId) {
        RestClient restClient = RestClient.create();
        ResponseEntity<String> walletResponse = restClient.get()
                .uri("http://localhost:8081/wallet/{id}", walletId)
                .retrieve()
                .toEntity(String.class);

        return walletResponse.getBody();
    }

    public static void validateWallet(String walletPayload) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(walletPayload);

        String walletId = jsonNode.get("id").asString();
        String accountType = jsonNode.get("accountType").asString();
        String status = jsonNode.get("status").asString();

        // check if wallet exists
        if (walletId.isBlank() || walletId.equals("null")) {
            throw new IllegalArgumentException("wallet not found");
        }

        // reject EXPENSE & REVENUE wallet for payment request
        if (accountType.equalsIgnoreCase("EXPENSE") || accountType.equalsIgnoreCase("REVENUE")) {
            throw new IllegalArgumentException("cannot use " + accountType + " type wallet for payment request");
        }

        // wallet must be in active status
        if (!status.equalsIgnoreCase("ACTIVE")) {
            throw new IllegalArgumentException("wallet must be ACTIVE to make a payment request");
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
