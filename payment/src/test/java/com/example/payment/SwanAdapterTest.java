package com.example.payment;

import com.example.payment.connector.SwanAdapter;
import com.example.payment.domain.swan.onboarding.collection.RequestSupportingDocumentCollectionReviewResponse;
import com.example.payment.domain.swan.onboarding.collection.SupportingDocumentCollectionResponse;
import com.example.payment.domain.swan.onboarding.create.Address;
import com.example.payment.domain.swan.onboarding.create.CompanyOnboarding;
import com.example.payment.domain.swan.onboarding.create.accountAdmin.AccountAdmin;
import com.example.payment.domain.swan.onboarding.create.accountAdmin.AccountLanguage;
import com.example.payment.domain.swan.onboarding.create.accountAdmin.TypeOfRepresentation;
import com.example.payment.domain.swan.onboarding.create.accountInfo.AccountCountry;
import com.example.payment.domain.swan.onboarding.create.accountInfo.AccountInfo;
import com.example.payment.domain.swan.onboarding.create.companyInfo.BusinessActivityCategory;
import com.example.payment.domain.swan.onboarding.create.companyInfo.CompanyInfo;
import com.example.payment.domain.swan.onboarding.create.companyInfo.MonthlyPaymentVolume;
import com.example.payment.domain.swan.onboarding.create.companyInfo.RegulatoryClassification;
import com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual.*;
import com.example.payment.domain.swan.onboarding.create.payload.CreateCompanyOnboardingResponse;
import com.example.payment.domain.swan.onboarding.document.SubmitSupportingDocument;
import com.example.payment.domain.swan.onboarding.document.SupportingDocumentPurpose;
import com.example.payment.domain.swan.onboarding.document.SupportingDocumentType;
import com.example.payment.domain.swan.onboarding.finalize.FinalizeAccountHolderOnboardingResponse;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.domain.swan.onboarding.retrieve.OnboardingEdge;
import com.example.payment.domain.swan.onboarding.retrieve.OnboardingErrors;
import com.example.payment.domain.swan.onboarding.update.UpdateCompanyOnboardingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class SwanAdapterTest {
    @Autowired
    private SwanAdapter swanAdapter;

    @Test
    void shouldCreateCompanyOnboarding() {
        // prepare documents & params
        // AccountInfo
        AccountInfo accountInfo = new AccountInfo(
                "ACME", AccountCountry.ESP
        );

        // AccountAdmin
        AccountAdmin accountAdmin = new AccountAdmin(
                "example@acme.com", AccountLanguage.ES, TypeOfRepresentation.LEGAL_REPRESENTATIVE
        );

        // CompanyInfo ** retrieve legalform code from query **
        CompanyInfo companyInfo = new CompanyInfo(
                "Acme",
                BusinessActivityCategory.TELECOMMUNICATION_IT_AND_INFORMATION_SERVICES,
                "provide telecommunication service",
                "123456789",
                "HRQA",
                MonthlyPaymentVolume.BETWEEN_10000_AND_50000,
                RegulatoryClassification.NON_FINANCIAL_ACTIVE,
                new Address("Plaça catalunya", "Barcelona", "ESP", "08002"),
                new RelatedIndividual(
                        RelatedIndividualType.LegalRepresentativeAndUltimateBeneficialOwner,
                        "Jane",
                        "Doe",
                        Sex.MALE,
                        new BirthInfo(LocalDate.parse("1990-03-03"), "ESP", "Barcelona", "08024"),
                        new Address("Plaça catalunya", "Barcelona", "ESP", "08002"),
                        "ESP",
                        new UnitedStatesTaxInfo(false, "123456789"),
                        "Y1234567Z",
                        new UltimateBeneficialOwner(
                                UltimateBeneficialOwnerQualificationType.OWNERSHIP,
                                new OwnerShip(UltimateBeneficialOwnerOwnershipType.DIRECT, 100f)
                        ),
                        new LegalRepresentative(List.of("Regulatory Compliance"))
                )
        );

        CompanyOnboarding companyOnboarding = new CompanyOnboarding(accountInfo, accountAdmin, companyInfo);
        // open new swan account
        CreateCompanyOnboardingResponse response = swanAdapter.createCompanyOnboarding(companyOnboarding);


//        DocumentContext documentContext = JsonPath.parse(response);
//
//        String accountId = documentContext.read("$.id");
//        String status = documentContext.read("$.statusInfo.status");

        assertThat(response.onboarding().id()).isNotBlank();
        assertThat(response.onboarding().statusInfo().status()).isEqualTo("Valid");
    }

    @Test
    void shouldRetrieveRecentOnboardingInfo() {
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge recentOnboarding = onboardingList.edges().get(0);
        assertThat(recentOnboarding.node().id()).isEqualTo("2c517a22-ec5d-49af-87b8-c065ea29fbf6");
    }

    @Test
    void shouldSubmitSupportingDocumentForReview() {
        // get missing but required field
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge recentOnboarding = onboardingList.edges().get(0);
        String onboardingId = recentOnboarding.node().id();

        // upload missing supporting documents //
        // retrieve collectionId
        SupportingDocumentCollectionResponse collectionResponse = swanAdapter.getCollectionId(onboardingId);
        String collectionId = collectionResponse.id();

        assertThat(collectionId).isEqualTo("79f69fa7-c17e-47ac-8035-2ab34188547e");
        // sworn statement
        SubmitSupportingDocument swornStatement = new SubmitSupportingDocument(collectionId, "Sworn statement", SupportingDocumentPurpose.SWORN_STATEMENT, SupportingDocumentType.SWORN_STATEMENT);
        swanAdapter.uploadOnboardingDocument(swornStatement);

        // UBO declaration
        SubmitSupportingDocument uboDeclaration = new SubmitSupportingDocument(collectionId, "UBO declaration", SupportingDocumentPurpose.UBO_DECLARATION, SupportingDocumentType.UBO_DECLARATION);
        swanAdapter.uploadOnboardingDocument(uboDeclaration);

        // request collection review
        RequestSupportingDocumentCollectionReviewResponse reviewResponse = swanAdapter.requestDocumentReview(collectionId);

        assertThat(reviewResponse.supportingDocumentCollection().statusInfo().status()).isEqualTo("PendingReview");
    }

    @Test
    void shouldUpdateOnboardingMissingData() {
        // get missing but required field
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge recentOnboarding = onboardingList.edges().get(0);
        String onboardingId = recentOnboarding.node().id();

        if (recentOnboarding.node().statusInfo().status().equalsIgnoreCase("Invalid")) {
            for (OnboardingErrors err : recentOnboarding.node().statusInfo().errors()) {
                System.out.println("field: " + err.field() + " /error: " + err.errors().get(0));
            }
        }

        // have to submit whole related individual to update
        // manual input for now
        AccountInfo accountInfo = new AccountInfo(
                "ACME", null
        );

        // AccountAdmin
        AccountAdmin accountAdmin = new AccountAdmin(
                "example@acme.com", AccountLanguage.ES, TypeOfRepresentation.LEGAL_REPRESENTATIVE
        );

        // manual input for now
        CompanyInfo companyInfo = new CompanyInfo(
                "Acme",
                BusinessActivityCategory.TELECOMMUNICATION_IT_AND_INFORMATION_SERVICES,
                "provide telecommunication service",
                "123456789",
                "HRQA",
                MonthlyPaymentVolume.BETWEEN_10000_AND_50000,
                RegulatoryClassification.NON_FINANCIAL_ACTIVE,
                new Address("Plaça catalunya", "Barcelona", "ESP", "08002"),
                new RelatedIndividual(
                        RelatedIndividualType.LegalRepresentativeAndUltimateBeneficialOwner,
                        "Jane",
                        "Doe",
                        Sex.MALE,
                        new BirthInfo(LocalDate.parse("1990-03-03"), "ESP", "Barcelona", "08024"),
                        new Address("Plaça catalunya", "Barcelona", "ESP", "08002"),
                        "ESP",
                        new UnitedStatesTaxInfo(false, "123456789"),
                        "Y1234567Z",
                        new UltimateBeneficialOwner(
                                UltimateBeneficialOwnerQualificationType.OWNERSHIP,
                                new OwnerShip(UltimateBeneficialOwnerOwnershipType.DIRECT, 100f)
                        ),
                        new LegalRepresentative(List.of("Regulatory Compliance"))
                )
        );
        CompanyOnboarding updatedOnboarding = new CompanyOnboarding(accountInfo, accountAdmin, companyInfo);

        UpdateCompanyOnboardingResponse response = swanAdapter.updateCompanyOnboarding(onboardingId, updatedOnboarding);

        // still missing supporting documents
        assertThat(response.onboarding().statusInfo().status()).isEqualTo("Valid");
    }

    // already finalized -> test failed
    @Test
    void shouldFinalizeCompanyOnboarding() {
        // retrieve onboarding id
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge recentOnboarding = onboardingList.edges().get(0);
        String onboardingId = recentOnboarding.node().id();

        FinalizeAccountHolderOnboardingResponse response = swanAdapter.finalizeCompanyOnaboarding(onboardingId);

        assertThat(response.onboarding().account().id()).isNotBlank();
        assertThat(response.onboarding().account().IBAN()).isBlank();
    }

    // test with existing onboardingId
    @Test
    void shouldCreateExternalAccountFromOnboarding() {
        AccountHolderOnboardingsResponse onboardingList = swanAdapter.getCompanyOnboarding();
        OnboardingEdge finalizedOnboarding = onboardingList.edges().get(0);

        assertThat(finalizedOnboarding.node().statusInfo().status()).isEqualTo("Finalized");
        assertThat(finalizedOnboarding.node().account().id()).isNotBlank();
        assertThat(finalizedOnboarding.node().account().IBAN()).isNotBlank();
    }
}
