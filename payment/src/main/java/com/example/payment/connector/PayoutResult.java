package com.example.payment.connector;

import lombok.Data;

// response from swan api
@Data
public class PayoutResult {
    private String referenceId;
    private String status;

    private PayoutResult(String referenceId, String status) {
        this.referenceId = referenceId;
        this.status = status;
    }

    public static PayoutResult success(String referenceId) {
        return new PayoutResult(referenceId, "Upcoming");
    }

    public static PayoutResult rejected(String referenceId) {
        return new PayoutResult(referenceId, "Rejected");
    }
}
