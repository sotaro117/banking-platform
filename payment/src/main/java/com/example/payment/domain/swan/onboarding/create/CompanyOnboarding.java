package com.example.payment.domain.swan.onboarding.create;

import com.example.payment.domain.swan.onboarding.create.accountAdmin.AccountAdmin;
import com.example.payment.domain.swan.onboarding.create.accountInfo.AccountInfo;
import com.example.payment.domain.swan.onboarding.create.companyInfo.CompanyInfo;

public record CompanyOnboarding(
        AccountInfo accountInfo,
        AccountAdmin accountAdmin,
        CompanyInfo company
) {
}