package com.rentalapp.payment;

import com.rentalapp.payment.dto.PaymentIntentResponse;
import com.rentalapp.user.User;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;

    @PostMapping("/bookings/{bookingId}/initiate")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentIntentResponse> initiate(
            @AuthenticationPrincipal User client,
            @PathVariable UUID bookingId) {
        return ResponseEntity.ok(paymentService.initiate(client.getId(), bookingId));
    }

    /**
     * Public endpoint (see SecurityConfig) — Stripe calls this directly, so it can't carry a
     * JWT. Trust is instead established by verifying the Stripe-Signature header below.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = stripeService.verifyAndParseWebhookEvent(payload, sigHeader);
            paymentService.handleWebhookEvent(event);
            return ResponseEntity.ok("received");
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid signature");
        }
    }
}
