package com.example.payment.connector;

import com.example.payment.connector.util.GraphqlRequest;
import com.example.payment.domain.swan.onboarding.collection.RequestSupportingDocumentCollectionReviewResponse;
import com.example.payment.domain.swan.onboarding.collection.SupportingDocumentCollectionResponse;
import com.example.payment.domain.swan.onboarding.create.CompanyOnboarding;
import com.example.payment.domain.swan.onboarding.document.GenerateSupportDocumentUrlResponse;
import com.example.payment.domain.swan.onboarding.document.GenerateSupportDocumentUrlResponseField;
import com.example.payment.domain.swan.onboarding.document.SubmitSupportingDocument;
import com.example.payment.domain.swan.onboarding.create.payload.CreateCompanyOnboardingResponse;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.domain.swan.onboarding.update.UpdateCompanyOnboardingResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class SwanAdapter implements RailAdapter{
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.swan.io/sandbox-partner/graphql")
//            .defaultHeader("x-swan-user-id", "d47a3543-ba21-4773-904b-b263739f5113")
            .defaultHeader("Authorization", "Bearer GkZx5hCpcwS-4B0SJQMZTgPvVXKWyrRQlDCNfHSpag4.ARlWV-A5LkciX0x3vdA_5jo_1-Xt9K4JvQWeEFonAn4")
            .build();

    private final HttpSyncGraphQlClient client = HttpSyncGraphQlClient.builder(restClient).build();

    @Override
    public PayoutResult send(PaymentInstruction instruction) {
        // prepare body to make api call
        String document = """
                mutation SepaDefault {
                  initiateCreditTransfers(
                    input: {
                      idempotencyKey: "$idempotencyKey"
                      consentRedirectUrl: "$YOUR_REDIRECT_URL"
                      accountId: "$accountId"
                      creditTransfers: {
                        amount: { value: "$amount", currency: "$currency" }
                        sepaBeneficiary: {
                          iban: "$iban"
                          name: "$name"
                          isMyOwnIban: false
                          save: false
                        }
                        mode: Regular
                      }
                    }
                  ) {
                    ... on InitiateCreditTransfersSuccessPayload {
                      __typename
                      payment {
                        createdAt
                        id
                        statusInfo {
                          ... on PaymentConsentPending {
                            __typename
                            consent {
                              consentUrl
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """;
        Map<String, Object> variables = new HashMap<>();
        variables.put("idempotencyKey", instruction.getIdempotencyKey());
        variables.put("YOUR_REDIRECT_URL", "");
        variables.put("accountId", instruction.getAccountId());
        variables.put("amount", instruction.getAmount());
        variables.put("currency", instruction.getCurrency());
        variables.put("iban", instruction.getIban());
        variables.put("name", instruction.getName());

        // call api + get response
//        var payment = client.document(document)
//                .variables(variables)
//                .retrieveSync()
//                .toEntity();

        // set PayoutResult

        // mock
        PayoutResult result = PayoutResult.success("mock-ref");
        return result;
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
}
