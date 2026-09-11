package com.rentalapp.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BookingRequest(
        @NotNull UUID propertyId,
        @NotNull @Future LocalDate startDate,
        @NotNull @Future LocalDate endDate
) {
}
