package com.example.payment.connector.util;

public class GraphqlRequest {
    public final static String CREATE_COMPANY_ONBOARDING = """
                mutation CreateCompanyOnboarding(
                    $input: CreateCompanyAccountHolderOnboardingInput!
                 ){
                  createCompanyAccountHolderOnboarding(
                    input: $input
                  ) {
                    ... on CreateCompanyAccountHolderOnboardingSuccessPayload {
                      __typename
                      onboarding {
                        id
                        statusInfo {
                          status
                        }
                        supportingDocumentCollection {
                          requiredSupportingDocumentPurposes {
                            acceptableSupportingDocumentTypes
                            name
                          }
                          statusInfo {
                            status
                          }
                        }
                      }
                    }
                  }
                }
                """;

    public final static String MONITOR_ONBOARDING = """
                query MonitorOnboarding {
                  accountHolderOnboardings(first: 10) {
                    edges {
                      node {
                        ... on CompanyAccountHolderOnboarding {
                          id
                          createdAt
                          onboardingUrl
                          statusInfo {
                            status
                            ... on OnboardingInvalidStatusInfo {
                              __typename
                              errors {
                                field
                                errors
                              }
                              status
                            }
                          }
                          accountAdmin {
                            email
                          }
                          company {
                            name
                          }
                          account {
                            id
                            IBAN
                          }
                        }
                      }
                    }
                    totalCount
                  }
                }
                """;

    public final static String UPDATE_COMPANY_ONBOARDING = """
                mutation UpdateCompanyOnboarding(
                    $input: UpdateCompanyAccountHolderOnboardingInput! 
                 ){
                  updateCompanyAccountHolderOnboarding(
                    input: $input
                  ) {
                    ... on UpdateCompanyAccountHolderOnboardingSuccessPayload {
                      __typename
                      onboarding {
                        statusInfo {
                          status
                        }
                      }
                    }
                    ... on OnboardingNotFoundRejection {
                      __typename
                    }
                    ... on OnboardingAlreadyFinalizedRejection {
                      __typename
                    }
                  }
                }
                """;

    public final static String GET_ONBOARDING_COLLECTION_INFO = """
            query getOnboardingCollectionInfo(
                $onboardingId: ID!
             ){
              accountHolderOnboarding(id: $onboardingId) {
                ... on CompanyAccountHolderOnboarding {
                  supportingDocumentCollection {
                    id
                  }
                }
              }
            }
            """;

    public final static String GENERATE_UPLOAD_URL = """
                mutation GenerateUploadUrl(
                    $input: GenerateSupportingDocumentUploadUrlInput!
                 ){
                  generateSupportingDocumentUploadUrl(
                    input: $input
                  ) {
                    ... on GenerateSupportingDocumentUploadUrlSuccessPayload {
                      __typename
                      upload {
                        fields {
                          key
                          value
                        }
                        url
                      }
                    }
                  }
                }
                """;

    public final static String REQUEST_COLLECTION_REVIEW = """
                mutation RequestCollectionReview(
                    $collectionId: ID!
                 ){
                  requestSupportingDocumentCollectionReview(
                    input: { supportingDocumentCollectionId: $collectionId }
                  ) {
                    ... on RequestSupportingDocumentCollectionReviewSuccessPayload {
                      __typename
                      supportingDocumentCollection {
                        id
                        statusInfo {
                          status
                        }
                      }
                    }
                    ... on ForbiddenRejection {
                      __typename
                      message
                    }
                    ... on SupportingDocumentCollectionNotFoundRejection {
                      id
                      message
                    }
                    ... on SupportingDocumentCollectionStatusNotAllowedRejection {
                      __typename
                      message
                      newStatus
                      oldStatus
                    }
                  }
                }
                """;

    public final static String FINALIZE_ONBOARDING = """
            mutation FinalizeOnboarding(
                $onboardingId: String!
             ){
              finalizeAccountHolderOnboarding(input: { onboardingId: $onboardingId }) {
                ... on FinalizeAccountHolderOnboardingSuccessPayload {
                  __typename
                  onboarding {
                    ... on CompanyAccountHolderOnboarding {
                      id
                      account {
                        id
                        IBAN
                      }
                      statusInfo {
                        status
                      }
                    }
                  }
                }
                ... on OnboardingNotFoundRejection {
                  __typename
                }
                ... on OnboardingAlreadyFinalizedRejection {
                  __typename
                }
                ... on OnboardingNotReadyForFinalizationRejection {
                  __typename
                }
              }
            }
            """;
}
