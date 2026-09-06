package com.example.payment.domain.swan.onboarding.document;

public enum SupportingDocumentPurpose {
    ASSOCIATION_REGISTRATION("AssociationRegistration"),
    BANKING("Banking"),
    COMPANY_REGISTRATION("CompanyRegistration"),
    GENERAL_ASSEMBLY_MINUTES("GeneralAssemblyMinutes"),
    OTHER("Other"),
    POWER_OF_ATTORNEY("PowerOfAttorney"),
    PROOF_OF_BUSINESS_ACTIVITY("ProofOfBusinessActivity"),
    PROOF_OF_COMPANY_ADDRESS("ProofOfCompanyAddress"),
    PROOF_OF_COMPANY_INCOME("ProofOfCompanyIncome"),
    PROOF_OF_IDENTITY("ProofOfIdentity"),
    PROOF_OF_INDIVIDUAL_ADDRESS("ProofOfIndividualAddress"),
    PROOF_OF_INDIVIDUAL_INCOME("ProofOfIndividualIncome"),
    PROOF_OF_ORIGIN_OF_FUNDS("ProofOfOriginOfFunds"),
    SIGNED_STATUS("SignedStatus"),
    UBO_DECLARATION("UBODeclaration"),
    US_PERSON_STATUS_DECLARATION("USPersonStatusDeclaration"),
    SWORN_STATEMENT("SwornStatement"),
    LEGAL_REPRESENTATIVE_PROOF_OF_IDENTITY("LegalRepresentativeProofOfIdentity"),
    ULTIMATE_BENEFICIAL_OWNER_PROOF_OF_IDENTITY("UltimateBeneficialOwnerProofOfIdentity"),
    NIF_ACCREDITATION_CARD("NIFAccreditationCard"),
    PRESIDENT_DECISION_OF_APPOINTMENT("PresidentDecisionOfAppointment"),
    ADMINISTRATOR_DECISION_OF_APPOINTMENT("AdministratorDecisionOfAppointment"),
    FINANCIAL_STATEMENTS("FinancialStatements"),
    ULTIMATE_BENEFICIAL_OWNER_PROOF_OF_ADDRESS("UltimateBeneficialOwnerProofOfAddress"),
    PERSONAL_INCOME("PersonalIncome"),
    PERSONAL_SAVINGS("PersonalSavings"),
    INVESTMENT("Investment"),
    DONATION("Donation"),
    INHERITANCE("Inheritance"),
    REAL_ESTATE_INCOME("RealEstateIncome"),
    GAMBLING_PRIZE_WINNINGS("GamblingPrizeWinnings"),
    TRADE("Trade"),
    COMPANY_TREASURY("CompanyTreasury"),
    COMPANY_OBLIGATIONS("CompanyObligations"),
    PEP_DECLARATION("PepDeclaration"),
    COMPANY_FORMATION_REGISTRATION("CompanyFormationRegistration");

    public final String label;

    SupportingDocumentPurpose(String label) {
        this.label = label;
    }
}
