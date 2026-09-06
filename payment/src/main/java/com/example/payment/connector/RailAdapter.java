package com.example.payment.connector;

import com.example.payment.domain.swan.onboarding.collection.RequestSupportingDocumentCollectionReviewResponse;
import com.example.payment.domain.swan.onboarding.collection.SupportingDocumentCollectionResponse;
import com.example.payment.domain.swan.onboarding.create.CompanyOnboarding;
import com.example.payment.domain.swan.onboarding.document.SubmitSupportingDocument;
import com.example.payment.domain.swan.onboarding.create.payload.CreateCompanyOnboardingResponse;
import com.example.payment.domain.swan.onboarding.retrieve.AccountHolderOnboardingsResponse;
import com.example.payment.domain.swan.onboarding.update.UpdateCompanyOnboardingResponse;

import java.util.List;
import java.util.Map;

public interface RailAdapter {
    PayoutResult send(PaymentInstruction instruction);
    CreateCompanyOnboardingResponse createCompanyOnboarding(CompanyOnboarding onboarding);
//    AccountHolderOnboardingsResponse getCompanyOnboarding();
    UpdateCompanyOnboardingResponse updateCompanyOnboarding(String onboardingId, CompanyOnboarding onboarding);
    void uploadOnboardingDocument(SubmitSupportingDocument supportingDocument);
    SupportingDocumentCollectionResponse getCollectionId(String onboardingId);
    RequestSupportingDocumentCollectionReviewResponse requestDocumentReview(String collectionId);
}
