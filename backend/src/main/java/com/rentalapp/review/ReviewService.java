package com.rentalapp.review;

import com.rentalapp.booking.Booking;
import com.rentalapp.booking.BookingRepository;
import com.rentalapp.booking.BookingStatus;
import com.rentalapp.common.exception.BadRequestException;
import com.rentalapp.review.dto.ReviewRequest;
import com.rentalapp.review.dto.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ReviewResponse create(UUID clientId, ReviewRequest request) {
        if (reviewRepository.existsByPropertyIdAndClientId(request.propertyId(), clientId)) {
            throw new BadRequestException("You have already reviewed this property");
        }

        boolean hasCompletedStay = bookingRepository.findByClientId(clientId).stream()
                .filter(b -> b.getPropertyId().equals(request.propertyId()))
                .map(Booking::getStatus)
                .anyMatch(status -> status == BookingStatus.COMPLETED);

        if (!hasCompletedStay) {
            throw new BadRequestException("You can only review properties you have stayed at");
        }

        Review review = Review.builder()
                .propertyId(request.propertyId())
                .clientId(clientId)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return ReviewResponse.from(reviewRepository.save(review));
    }

    public List<ReviewResponse> getByProperty(UUID propertyId) {
        return reviewRepository.findByPropertyId(propertyId).stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
