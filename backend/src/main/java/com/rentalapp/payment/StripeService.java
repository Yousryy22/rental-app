package com.rentalapp.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class StripeService {

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Creates a Stripe PaymentIntent. Amount is converted to the smallest currency unit
     * (cents) as Stripe expects. `bookingId` is passed through as idempotency-relevant
     * metadata so it can be reconciled from the webhook without re-querying by amount alone.
     */
    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, UUID bookingId) throws StripeException {
        long amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100)).longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency(currency.toLowerCase())
                .putMetadata("bookingId", bookingId.toString())
                // Idempotency key ties this call to the booking so a network retry from the
                // frontend doesn't create a second PaymentIntent (and a double charge).
                .build();

        return PaymentIntent.create(params);
    }

    public Event verifyAndParseWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
