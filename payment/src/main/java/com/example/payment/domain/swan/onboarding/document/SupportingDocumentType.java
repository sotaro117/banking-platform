package com.example.payment.domain.swan.onboarding.document;

public enum SupportingDocumentType {
    SELFIE("Selfie"),
    PASSPORT("Passport"),
    NATIONAL_ID_CARD("NationalIdCard"),
    RESIDENT_PERMIT("ResidentPermit"),
    DRIVING_LICENSE("DrivingLicense"),
    UTILITY_BILL("UtilityBill"),
    PHONE_BILL("PhoneBill"),
    RENT_RECEIPT("RentReceipt"),
    HOME_INSURANCE("HomeInsurance"),
    INCOME_TAX_RETURN("IncomeTaxReturn"),
    PAY_SLIP("PaySlip"),
    REGISTER_EXTRACT("RegisterExtract"),
    ARTICLES_OF_INCORPORATION("ArticlesOfIncorporation"),
    CAPITAL_SHARE_DEPOSIT_CERTIFICATE("CapitalShareDepositCertificate"),
    UBO_DECLARATION("UBODeclaration"),
    JOAFFE_EXTRACT("JOAFFEExtract"),
    COMPANY_LEASE_AGREEMENT("CompanyLeaseAgreement"),
    BANK_STATEMENT("BankStatement"),
    BANK_ACCOUNT_DETAILS("BankAccountDetails"),
    POWER_OF_ATTORNEY("PowerOfAttorney"),
    CORPORATE_INCOME_TAX_RETURN("CorporateIncomeTaxReturn"),
    OTHER("Other"),
    BY_LAWS("ByLaws"),
    ACCOUNT_STATEMENT("AccountStatement"),
    DEED_OF_DONATION("DeedOfDonation"),
    DEED_OF_SALE("DeedOfSale"),
    DEED_OF_SUCCESSION("DeedOfSuccession"),
    LOAN_CONTRACT("LoanContract"),
    NOTARIAL_DEED("NotarialDeed"),
    SWORN_STATEMENT("SwornStatement"),
    MEETING_MINUTES("MeetingMinutes"),
    NIF_ACCREDITATION_CARD("NIFAccreditationCard"),
    DECISION_OF_APPOINTMENT("DecisionOfAppointment"),
    FINANCIAL_STATEMENTS("FinancialStatements"),
    WINNINGS_CERTIFICATE("WinningsCertificate"),
    INVOICE("Invoice"),
    PEP_DECLARATION("PepDeclaration"),
    W9("W9"),
    W8("W8"),
    CERTIFICATE_OF_LOSS_OF_NATIONALITY("CertificateOfLossOfNationality");

    public final String label;

    SupportingDocumentType(String label) {
        this.label = label;
    }
}
