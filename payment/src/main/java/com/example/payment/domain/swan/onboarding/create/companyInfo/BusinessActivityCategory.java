package com.example.payment.domain.swan.onboarding.create.companyInfo;

public enum BusinessActivityCategory {
    ACCOMMODATION_AND_FOOD_SERVICE("AccommodationAndFoodService"),
    ADMINISTRATIVE_AND_SUPPORT_SERVICE_ACTIVITIES("AdministrativeAndSupportServiceActivities"),
    AGRICULTURE_FORESTRY_AND_FISHING("AgricultureForestryAndFishing"),
    ARTS_SPORTS_AND_RECREATION("ArtsSportsAndRecreation"),
    CONSTRUCTION("Construction"),
    EDUCATION("Education"),
    ELECTRICITY_GAS_STEAM_AND_AIR_CONDITIONING_SUPPLY("ElectricityGasSteamAndAirConditioningSupply"),
    EXTRATERRITORIAL_ORGANISATIONS_AND_BODIES("ExtraterritorialOrganisationsAndBodies"),
    FINANCIAL_AND_INSURANCE_ACTIVITIES("FinancialAndInsuranceActivities"),
    HOUSEHOLD_EMPLOYER_AND_OWN_USE_ACTIVITIES("HouseholdEmployerAndOwnUseActivities"),
    HUMAN_HEALTH_AND_SOCIAL_WORK("HumanHealthAndSocialWork"),
    MANUFACTURING("Manufacturing"),
    MINING_AND_QUARRYING("MiningAndQuarrying"),
    OTHER_SERVICE_ACTIVITIES("OtherServiceActivities"),
    PROFESSIONAL_SCIENTIFIC_AND_TECHNICAL_ACTIVITIES("ProfessionalScientificAndTechnicalActivities"),
    PUBLIC_ADMINISTRATION_AND_DEFENCE_SOCIAL_SECURITY("PublicAdministrationAndDefenceSocialSecurity"),
    PUBLISHING_BROADCASTING_AND_CONTENT_PRODUCTION_AND_DISTRIBUTION("PublishingBroadcastingAndContentProductionAndDistribution"),
    REAL_ESTATE_ACTIVITIES("RealEstateActivities"),
    TELECOMMUNICATION_IT_AND_INFORMATION_SERVICES("TelecommunicationItAndInformationServices"),
    TRANSPORTATION_AND_STORAGE("TransportationAndStorage"),
    WATER_SUPPLY_SEWERAGE_WASTE_MANAGEMENT_AND_REMEDIATION("WaterSupplySewerageWasteManagementAndRemediation"),
    WHOLESALE_AND_RETAIL_TRADE("WholesaleAndRetailTrade");

    public final String value;

    private BusinessActivityCategory(String value) {
        this.value = value;
    }
}
