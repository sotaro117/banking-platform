package com.example.payment.connector;

import com.example.payment.connector.util.GraphqlRequest;
import com.example.payment.domain.swan.consent.grantS2s.GrantS2sResponsePayload;
import com.example.payment.domain.swan.consent.retrieve.ConsentResponsePayload;
import com.example.payment.domain.swan.onboarding.collection.RequestSupportingDocumentCollectionReviewResponse;
import com.example.payment.domain.swan.onboarding.collection.SupportingDocumentCollectionResponse;
import com.example.payment.domain.swan.onboarding.create.CompanyOnboarding;
import com.example.payment.domain.swan.onboarding.document.GenerateSupportDocumentUrlResponse;
import com.example.payment.domain.swan.onboarding.document.GenerateSupportDocumentUrlResponseField;
import com.example.payment.domain.swan.onboarding.document.SubmitSupportingDocument;
import com.example.payment.domain.swan.onboarding.create.payload.CreateCompanyOnboardingResponse;
import com.example.payment.domain.swan.onboarding.finalize.FinalizeAccountHolderOnboardingResponse;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.domain.swan.onboarding.update.UpdateCompanyOnboardingResponse;
import com.example.payment.domain.swan.sepaTransfer.beneficiary.TrustedBefeficiary;
import com.example.payment.domain.swan.sepaTransfer.transfer.IniciateTransferResponse;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SwanAdapter implements RailAdapter{
    // project level access
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.swan.io/sandbox-partner/graphql")
            .defaultHeader("Authorization", "Bearer project token")
            .build();

    // user level access
    private final RestClient restClientUserAccess = RestClient.builder()
            .baseUrl("https://api.swan.io/sandbox-partner/graphql")
            .defaultHeader("Authorization", "Bearer user access token")
            .build();

    private final HttpSyncGraphQlClient client = HttpSyncGraphQlClient.builder(restClient).build();

    private final HttpSyncGraphQlClient clientUserAccess = HttpSyncGraphQlClient.builder(restClientUserAccess).build();

    // test key
    @Value("${swan.s2s.private-jwk")
    private String privateKey;

    @Override
    public IniciateTransferResponse send(PaymentInstruction instruction) {
        // prepare body to make api call
        String document = GraphqlRequest.SEPA_DEFAULT;
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of(
                "idempotencyKey", instruction.getIdempotencyKey(),
                "consentRedirectUrl", "http://localhost:8082/payment/consent/callback",
                "accountId", instruction.getAccountId(),
                "creditTransfers", Map.of(
                        "amount", Map.of(
                                "value", instruction.getAmount(),
                                "currency", instruction.getCurrency()
                        ),
                        "sepaBeneficiary", Map.of(
                                "iban", instruction.getIban(),
                                "name", instruction.getName(),
                                "save", false
                        ),
                        "mode", "Regular"
                )
        ));

//        IniciateTransferResponse result = client.document(document)
//                .variables(variables)
//                .retrieveSync("initiateCreditTransfers")
//                .toEntity(IniciateTransferResponse.class);

        IniciateTransferResponse result = clientUserAccess.document(document)
                .variables(variables)
                .retrieveSync("initiateCreditTransfers")
                .toEntity(IniciateTransferResponse.class);

        processConsent(result.payment().statusInfo().consent().consentUrl());

        if (result.payment().statusInfo().status().equalsIgnoreCase("Rejected")) {
            throw new RuntimeException("Payment rejected");
        }

        return result;
    }

    private void processConsent(String url) {
        URI uri = URI.create(url);
        String query = uri.getQuery();
        String consentId = Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .filter(param -> param[0].equals("consentId"))
                .map(param -> param[1])
                .findFirst()
                .orElseThrow();

        String reqConsentDocument = GraphqlRequest.CONSENT;

        Map<String, Object> variables = new HashMap<>();
        variables.put("id", consentId);

        ConsentResponsePayload consent = clientUserAccess.document(reqConsentDocument)
                .variables(variables)
                .retrieveSync("consent")
                .toEntity(ConsentResponsePayload.class);

        try {
            String testPrivateKey = """
                    
                    """;
            ECKey privateECKey = ECKey.parse(privateKey);

            String signature = signConsentChallenge(consent.challenge(), privateECKey);

            String s2sDocument = GraphqlRequest.GRANT_S2S;

            Map<String, Object> s2sVariables = new HashMap<>();
            s2sVariables.put("input", Map.of(
                    "consentId", consentId,
                    "signature", signature
            ));

            Map<String, Object> response = clientUserAccess.document(s2sDocument)
                    .variables(s2sVariables)
                    .retrieveSync("grantConsentWithServerSignature")
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> consentResData = (Map<String, Object>) response.get("consent");


            if (!consentResData.get("status").equals("Accepted")) {
                throw new RuntimeException("Consent not accepted");
            }
        } catch (Exception ex) {
            System.out.println("consent processing error: " + ex);
            throw new RuntimeException("Error in processing consent");
        }
    }

    private String signConsentChallenge(
            String challenge,
            ECKey privateJwk
    ) throws Exception {
        Map<String, String> header = new LinkedHashMap<>();
        header.put("alg", "ES256");
        header.put("typ", "JWT");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("challenge", challenge);
        ObjectMapper objectMapper = new ObjectMapper();
        String headerJson = objectMapper.writeValueAsString(header);
        String payloadJson = objectMapper.writeValueAsString(payload);
        String encodedHeader = Base64URL.encode(
                headerJson.getBytes(StandardCharsets.UTF_8)
        ).toString();

        String encodedPayload = Base64URL.encode(
                payloadJson.getBytes(StandardCharsets.UTF_8)
        ).toString();

        String message = encodedHeader + "." + encodedPayload;

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .build();

        ECDSASigner signer = new ECDSASigner(
                privateJwk.toECPrivateKey()
        );

        Base64URL signature = signer.sign(
                jwsHeader,
                message.getBytes(StandardCharsets.UTF_8)
        );
        return message + "." + signature;

    }

    @Override
    public CreateCompanyOnboardingResponse createCompanyOnboarding(CompanyOnboarding onboarding) {
        String document = GraphqlRequest.CREATE_COMPANY_ONBOARDING;

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of(
                "accountInfo", Map.of(
                        "name", onboarding.accountInfo().name(),
                        "country", onboarding.accountInfo().country().name()
                ),
                "accountAdmin", Map.of(
                        "email", onboarding.accountAdmin().email(),
                        "preferredLanguage", onboarding.accountAdmin().preferredLanguage().label,
                        "typeOfRepresentation", onboarding.accountAdmin().typeOfRepresentation().label
                ),
                "company", Map.of(
                        "name", onboarding.company().companyName(),
                        "businessActivity", onboarding.company().businessActivity().value,
                        "businessActivityDescription", onboarding.company().businessActivityDescription(),
                        "registrationNumber", onboarding.company().registrationNumber(),
                        "legalFormCode", onboarding.company().legalFormCode(),
                        "monthlyPaymentVolume", onboarding.company().monthlyPaymentVolume().label,
                        "address", Map.of(
                                "addressLine1", onboarding.company().address().address(),
                                "city", onboarding.company().address().city(),
                                "country", onboarding.company().address().country(),
                                "postalCode", onboarding.company().address().postalCode()
                        ),
                        "regulatoryClassification", onboarding.company().regulatoryClassification(),
                        "relatedIndividuals", Map.of(
                                "type", onboarding.company().relatedIndividual().type().name(),
                                "firstName", onboarding.company().relatedIndividual().firstName(),
                                "lastName", onboarding.company().relatedIndividual().lastName(),
                                "sex", onboarding.company().relatedIndividual().sex().label,
                                "birthInfo", Map.of(
                                        "birthDate", onboarding.company().relatedIndividual().birthInfo().birthDate(),
                                        "country", onboarding.company().relatedIndividual().birthInfo().country(),
                                        "city", onboarding.company().relatedIndividual().birthInfo().city(),
                                        "postalCode", onboarding.company().relatedIndividual().birthInfo().postalCode()
                                ),
                                "address", Map.of(
                                        "addressLine1", onboarding.company().relatedIndividual().address().address(),
                                        "city", onboarding.company().relatedIndividual().address().city(),
                                        "country", onboarding.company().relatedIndividual().address().country(),
                                        "postalCode", onboarding.company().relatedIndividual().address().postalCode()
                                ),
                                "nationality", onboarding.company().relatedIndividual().nationality(),
                                "ultimateBeneficialOwner", Map.of(
                                        "qualificationType", onboarding.company().relatedIndividual().ultimateBeneficialOwner().qualificationType().label,
                                        "ownership", Map.of(
                                                "type", onboarding.company().relatedIndividual().ultimateBeneficialOwner().ownerShip().type().label,
                                                "totalPercentage", onboarding.company().relatedIndividual().ultimateBeneficialOwner().ownerShip().totalPercentage()
                                        )
                                ),
                                "unitedStatesTaxInfo", Map.of(
                                        "isUnitedStatesPerson", onboarding.company().relatedIndividual().unitedStatesTaxInfo().isUnitedStatesPerson(),
                                        "unitedStatesTaxIdentificationNumber", onboarding.company().relatedIndividual().unitedStatesTaxInfo().unitedStatesTaxIdentificationNumber()
                                ),
                                "legalRepresentative", Map.of(
                                        "roles", onboarding.company().relatedIndividual().legalRepresentative().roles()
                                )
                        )
                )
        ));

        CreateCompanyOnboardingResponse response = client.document(document)
                .variables(variables)
                .retrieveSync("createCompanyAccountHolderOnboarding")
                .toEntity(CreateCompanyOnboardingResponse.class); // String or class

        return response;
    }

    public AccountHolderOnboardingsResponse getCompanyOnboarding() {
        String document = GraphqlRequest.MONITOR_ONBOARDING;

        AccountHolderOnboardingsResponse response = client.document(document)
                .retrieveSync("accountHolderOnboardings")
                .toEntity(AccountHolderOnboardingsResponse.class);

        return response;
    }

    @Override
    public UpdateCompanyOnboardingResponse updateCompanyOnboarding(String onboardingId, CompanyOnboarding onboarding) {
        String document = GraphqlRequest.UPDATE_COMPANY_ONBOARDING;

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of(
                "onboardingId", onboardingId,
                "accountInfo", Map.of(
                        "name", onboarding.accountInfo().name()
                ),
                "accountAdmin", Map.of(
                        "email", onboarding.accountAdmin().email(),
                        "preferredLanguage", onboarding.accountAdmin().preferredLanguage().label,
                        "typeOfRepresentation", onboarding.accountAdmin().typeOfRepresentation().label
                ),
                "company", Map.of(
                        "name", onboarding.company().companyName(),
                        "businessActivity", onboarding.company().businessActivity().value,
                        "businessActivityDescription", onboarding.company().businessActivityDescription(),
                        "registrationNumber", onboarding.company().registrationNumber(),
                        "legalFormCode", onboarding.company().legalFormCode(),
                        "monthlyPaymentVolume", onboarding.company().monthlyPaymentVolume().label,
                        "address", Map.of(
                                "addressLine1", onboarding.company().address().address(),
                                "city", onboarding.company().address().city(),
                                "country", onboarding.company().address().country(),
                                "postalCode", onboarding.company().address().postalCode()
                        ),
                        "regulatoryClassification", onboarding.company().regulatoryClassification().label,
                        "relatedIndividuals", Map.of(
                                "type", onboarding.company().relatedIndividual().type().name(),
                                "firstName", onboarding.company().relatedIndividual().firstName(),
                                "lastName", onboarding.company().relatedIndividual().lastName(),
                                "sex", onboarding.company().relatedIndividual().sex().label,
                                "birthInfo", Map.of(
                                        "birthDate", onboarding.company().relatedIndividual().birthInfo().birthDate(),
                                        "country", onboarding.company().relatedIndividual().birthInfo().country(),
                                        "city", onboarding.company().relatedIndividual().birthInfo().city(),
                                        "postalCode", onboarding.company().relatedIndividual().birthInfo().postalCode()
                                ),
                                "address", Map.of(
                                        "addressLine1", onboarding.company().relatedIndividual().address().address(),
                                        "city", onboarding.company().relatedIndividual().address().city(),
                                        "country", onboarding.company().relatedIndividual().address().country(),
                                        "postalCode", onboarding.company().relatedIndividual().address().postalCode()
                                ),
                                "nationality", onboarding.company().relatedIndividual().nationality(),
                                "ultimateBeneficialOwner", Map.of(
                                        "qualificationType", onboarding.company().relatedIndividual().ultimateBeneficialOwner().qualificationType().label,
                                        "ownership", Map.of(
                                                "type", onboarding.company().relatedIndividual().ultimateBeneficialOwner().ownerShip().type().label,
                                                "totalPercentage", onboarding.company().relatedIndividual().ultimateBeneficialOwner().ownerShip().totalPercentage()
                                        )
                                ),
                                "unitedStatesTaxInfo", Map.of(
                                        "isUnitedStatesPerson", onboarding.company().relatedIndividual().unitedStatesTaxInfo().isUnitedStatesPerson(),
                                        "unitedStatesTaxIdentificationNumber", onboarding.company().relatedIndividual().unitedStatesTaxInfo().unitedStatesTaxIdentificationNumber()
                                ),
                                "legalRepresentative", Map.of(
                                        "roles", onboarding.company().relatedIndividual().legalRepresentative().roles()
                                )
                        )
                )
        ));

        UpdateCompanyOnboardingResponse response = client.document(document)
                .variables(variables)
                .retrieveSync("updateCompanyAccountHolderOnboarding")
                .toEntity(UpdateCompanyOnboardingResponse.class);

        return response;
    }

    @Override
    public void uploadOnboardingDocument(SubmitSupportingDocument supportingDocument) {
        String document = GraphqlRequest.GENERATE_UPLOAD_URL;

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of(
                "supportingDocumentCollectionId", supportingDocument.supportingDocumentCollectionId(),
                "filename", supportingDocument.filename(),
                "supportingDocumentPurpose", supportingDocument.supportingDocumentPurpose().label,
                "supportingDocumentType", supportingDocument.supportingDocumentType().label
        ));

        GenerateSupportDocumentUrlResponse generatedUrlKeyValues = client.document(document)
                .variables(variables)
                .retrieveSync("generateSupportingDocumentUploadUrl")
                .toEntity(GenerateSupportDocumentUrlResponse.class); // String or class


        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        for (GenerateSupportDocumentUrlResponseField keyValue: generatedUrlKeyValues.upload().fields()) {
            body.add(keyValue.key(), keyValue.value());
        }

        // with frontend + file upload -> stored in tmp/
        // String filePath = System.getProperty("java.io.tmpdir") + "/" + supportingDocument.filename();
//        Path filePath = Paths.get("payment/src/test/java/resources/", supportingDocument.filename() + ".pdf");
        Resource file = new ClassPathResource(supportingDocument.filename() + ".pdf");
        body.add("file", file);

        // debug block: retrieve the file size in bytes (from)//
        FormHttpMessageConverter converter = new FormHttpMessageConverter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HttpHeaders headers = new HttpHeaders();
        HttpOutputMessage outputMessage = new HttpOutputMessage() {
            @Override
            public OutputStream getBody() {
                return outputStream;
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };

        ClientHttpRequestInterceptor interceptor = (request, byteBody, execution) -> {
            System.out.println("===== REQUEST =====");

            System.out.println("Content-Type: " + request.getHeaders().getContentType());

            System.out.println("Content-Length: " + request.getHeaders().getContentLength());

            System.out.println("Transfer-Encoding: " + request.getHeaders().getFirst(HttpHeaders.TRANSFER_ENCODING));

            System.out.println("Actual serialized body length: " + byteBody.length);
            return execution.execute(request, byteBody);
        };

        RestClient restClientForSupportingDocument = RestClient.builder()
                .requestInterceptor(interceptor)
                .build();

        try {
            converter.write(body, MediaType.MULTIPART_FORM_DATA, outputMessage);

            byte[] requestBody = outputStream.toByteArray();
            // debug block: retrieve the file size in bytes (to)//

            String uri = generatedUrlKeyValues.upload().url();
            ResponseEntity<Void> response = restClientForSupportingDocument.post()
                    .uri(uri)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new RuntimeException("error when uploading supporting documents");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public SupportingDocumentCollectionResponse getCollectionId(String onboardingId) {
        String document = GraphqlRequest.GET_ONBOARDING_COLLECTION_INFO;

        Map<String, Object> variables = new HashMap<>();
        variables.put("onboardingId", onboardingId);
        SupportingDocumentCollectionResponse response = client.document(document)
                .variables(variables)
                .retrieveSync("accountHolderOnboarding.supportingDocumentCollection")
                .toEntity(SupportingDocumentCollectionResponse.class);

        return response;
    }

    @Override
    public RequestSupportingDocumentCollectionReviewResponse requestDocumentReview(String collectionId) {
        String document = GraphqlRequest.REQUEST_COLLECTION_REVIEW;

        Map<String, Object> variables = new HashMap<>();
        variables.put("collectionId", collectionId);

        RequestSupportingDocumentCollectionReviewResponse response = client.document(document)
                .variables(variables)
                .retrieveSync("requestSupportingDocumentCollectionReview")
                .toEntity(RequestSupportingDocumentCollectionReviewResponse.class);

        return response;
    }

    @Override
    public FinalizeAccountHolderOnboardingResponse finalizeCompanyOnaboarding(String onboardingId) {
        String document = GraphqlRequest.FINALIZE_ONBOARDING;

        Map<String, Object> variables = new HashMap<>();
        variables.put("onboardingId", onboardingId);

        FinalizeAccountHolderOnboardingResponse response = clientUserAccess.document(document)
                .variables(variables)
                .retrieveSync("finalizeAccountHolderOnboarding")
                .toEntity(FinalizeAccountHolderOnboardingResponse.class);

        return response;
    }

    // SEPA related (VoP -> real account number)
    @Override
    public TrustedBefeficiary addBeneficiary(String accountId, String iban, String name, String consentRedirectUrl) {
        String document = GraphqlRequest.ADD_BENEFICIARY;

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of(
                "accountId", accountId,
                "iban", iban,
                "name", name,
                "consentRedirectUrl", consentRedirectUrl
        ));

        TrustedBefeficiary response = clientUserAccess.document(document)
                .variables(variables)
                .retrieveSync("addTrustedSepaBeneficiary.trustedBeneficiary")
                .toEntity(TrustedBefeficiary.class);

        return response;
    }
}
