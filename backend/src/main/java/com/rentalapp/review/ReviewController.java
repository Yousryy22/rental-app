package com.rentalapp.review;

import com.rentalapp.review.dto.ReviewRequest;
import com.rentalapp.review.dto.ReviewResponse;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> create(
            @AuthenticationPrincipal User client,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(client.getId(), request));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponse>> byProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(reviewService.getByProperty(propertyId));
    }
}
