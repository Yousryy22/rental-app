package com.rentalapp.payment.dto;

import java.util.UUID;

public record PaymentIntentResponse(
        UUID paymentId,
        String clientSecret
) {
}
