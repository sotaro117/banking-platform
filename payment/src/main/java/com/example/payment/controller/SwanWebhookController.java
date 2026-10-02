package com.example.payment.controller;

import com.example.payment.domain.PaymentRequest;
import com.example.payment.domain.SwanEvent;
import com.example.payment.domain.enums.PaymentState;
import com.example.payment.service.PaymentRequestService;
import com.example.payment.service.SwanEventService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class SwanWebhookController {
    private final PaymentRequestService paymentRequestService;
    private final SwanEventService swanEventService;

    public SwanWebhookController(PaymentRequestService paymentRequestService, SwanEventService swanEventService) {
        this.paymentRequestService = paymentRequestService;
        this.swanEventService = swanEventService;
    }

    @PostMapping("/payment")
    public void handlePaymentProcess(@RequestBody Map<String, Object> payload) {
        System.out.println("Incoming request: " + payload);
        String eventType = (String) payload.get("eventType");
        System.out.println("Event type: " + eventType);
        String eventId = (String) payload.get("eventId");
        String resourceId = (String) payload.get("resourceId");
        Instant processedAt = Instant.parse((String) payload.get("eventDate"));
        // record swan event
        SwanEvent swanEvent = new SwanEvent(eventId, eventType, resourceId, processedAt);
        swanEventService.saveEvent(swanEvent);

        // retrieve the processed payment request
        PaymentRequest request = paymentRequestService.getRequestBySwanReference(resourceId);

        // set status depending on the webhook result
        if (eventType.equals("Transaction.Booked")) {
            request.setPaymentState(PaymentState.SETTLED);
            paymentRequestService.saveRequest(request);
            // fire events to notification + audit
            // change status in ledger transaction -> posted
        } else if (eventType.equals("Transaction.Rejected")) {
            // -> compensating logic
            paymentRequestService.requestReversal(request);
            // compensating -> reversal
            // payment request status -> reverse the transaction by interacting with ledger service
            // change status in ledger transaction -> reversed
        } else if (eventType.equals("Transaction.Pending")) {
            request.setPaymentState(PaymentState.PENDING);
            System.out.println("Pending request: " + request);
            paymentRequestService.saveRequest(request);
        }
    }

    @PostMapping("/consent")
    public void handleCriticalProcess(@RequestBody Map<String, Object> payload) {
        System.out.println("Incoming request: " + payload);
    }
}
