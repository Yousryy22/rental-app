package com.rentalapp.booking;

import com.rentalapp.booking.dto.BookingRequest;
import com.rentalapp.booking.dto.BookingResponse;
import com.rentalapp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> create(
            @AuthenticationPrincipal User client,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(client.getId(), request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> cancel(
            @AuthenticationPrincipal User client,
            @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancel(client.getId(), id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<BookingResponse>> myBookings(@AuthenticationPrincipal User client) {
        return ResponseEntity.ok(bookingService.getByClient(client.getId()));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<BookingResponse>> byProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(bookingService.getByProperty(propertyId));
    }
}
