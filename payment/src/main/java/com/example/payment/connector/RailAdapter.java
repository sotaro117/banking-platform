package com.example.payment.connector;

import com.example.payment.domain.swan.onboarding.collection.RequestSupportingDocumentCollectionReviewResponse;
import com.example.payment.domain.swan.onboarding.collection.SupportingDocumentCollectionResponse;
import com.example.payment.domain.swan.onboarding.create.CompanyOnboarding;
import com.example.payment.domain.swan.onboarding.document.SubmitSupportingDocument;
import com.example.payment.domain.swan.onboarding.create.payload.CreateCompanyOnboardingResponse;
import com.example.payment.domain.swan.onboarding.finalize.FinalizeAccountHolderOnboardingResponse;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.domain.swan.onboarding.update.UpdateCompanyOnboardingResponse;
import com.example.payment.domain.swan.sepaTransfer.beneficiary.TrustedBefeficiary;
import com.example.payment.domain.swan.sepaTransfer.transfer.IniciateTransferResponse;

public interface RailAdapter {
    // company onboarding
    CreateCompanyOnboardingResponse createCompanyOnboarding(CompanyOnboarding onboarding);
    AccountHolderOnboardingsResponse getCompanyOnboarding();
    UpdateCompanyOnboardingResponse updateCompanyOnboarding(String onboardingId, CompanyOnboarding onboarding);
    void uploadOnboardingDocument(SubmitSupportingDocument supportingDocument);
    SupportingDocumentCollectionResponse getCollectionId(String onboardingId);
    RequestSupportingDocumentCollectionReviewResponse requestDocumentReview(String collectionId);
    FinalizeAccountHolderOnboardingResponse finalizeCompanyOnaboarding(String onboardingId);

    // SEPA transaction
    TrustedBefeficiary addBeneficiary(String accountId, String iban, String name, String consentRedirectUrl);
    IniciateTransferResponse send(PaymentInstruction instruction);
}
