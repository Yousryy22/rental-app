package com.rentalapp.review.dto;

import com.rentalapp.review.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID propertyId,
        UUID clientId,
        Integer rating,
        String comment,
        Instant createdAt
) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId(), r.getPropertyId(), r.getClientId(),
                r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
