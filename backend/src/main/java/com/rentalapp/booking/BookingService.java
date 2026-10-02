package com.rentalapp.booking;

import com.rentalapp.booking.dto.BookingRequest;
import com.rentalapp.booking.dto.BookingResponse;
import com.rentalapp.common.exception.BadRequestException;
import com.rentalapp.common.exception.ForbiddenException;
import com.rentalapp.common.exception.ResourceNotFoundException;
import com.rentalapp.property.Property;
import com.rentalapp.property.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int DAYS_PER_MONTH = 30;

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public BookingResponse create(UUID clientId, BookingRequest request) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + request.propertyId()));

        List<Booking> overlapping = bookingRepository.findOverlapping(
                request.propertyId(), request.startDate(), request.endDate());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Property is not available for the selected dates");
        }

        BigDecimal totalAmount = computeTotal(property.getPricePerMonth(),
                request.startDate(), request.endDate());

        Booking booking = Booking.builder()
                .propertyId(property.getId())
                .clientId(clientId)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalAmount(totalAmount)
                .build(); // status defaults to PENDING until payment succeeds

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse cancel(UUID clientId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getClientId().equals(clientId)) {
            throw new ForbiddenException("You do not own this booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    public List<BookingResponse> getByClient(UUID clientId) {
        return bookingRepository.findByClientId(clientId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<BookingResponse> getByProperty(UUID propertyId) {
        return bookingRepository.findByPropertyId(propertyId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    private BigDecimal computeTotal(BigDecimal pricePerMonth, java.time.LocalDate start, java.time.LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        BigDecimal months = BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(DAYS_PER_MONTH), 4, RoundingMode.HALF_UP);
        return pricePerMonth.multiply(months).setScale(2, RoundingMode.HALF_UP);
    }
}
