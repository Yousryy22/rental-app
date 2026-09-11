package com.rentalapp.booking.dto;

import com.rentalapp.booking.Booking;
import com.rentalapp.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID propertyId,
        UUID clientId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        BigDecimal totalAmount
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getPropertyId(), b.getClientId(),
                b.getStartDate(), b.getEndDate(), b.getStatus(), b.getTotalAmount());
    }
}
