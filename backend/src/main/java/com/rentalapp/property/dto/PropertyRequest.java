package com.rentalapp.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PropertyRequest(
        @NotBlank String title,
        String description,
        @NotBlank String address,
        String city,
        @Positive BigDecimal pricePerMonth,
        @PositiveOrZero Integer bedrooms,
        @PositiveOrZero Integer bathrooms
) {
}
