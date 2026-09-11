package com.rentalapp.payment;

import com.rentalapp.booking.Booking;
import com.rentalapp.booking.BookingRepository;
import com.rentalapp.booking.BookingStatus;
import com.rentalapp.common.exception.BadRequestException;
import com.rentalapp.common.exception.ForbiddenException;
import com.rentalapp.common.exception.ResourceNotFoundException;
import com.rentalapp.payment.dto.PaymentIntentResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final StripeService stripeService;

    @Transactional
    public PaymentIntentResponse initiate(UUID clientId, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getClientId().equals(clientId)) {
            throw new ForbiddenException("You do not own this booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be paid for");
        }

        try {
            PaymentIntent intent = stripeService.createPaymentIntent(
                    booking.getTotalAmount(), "USD", booking.getId());

            Payment payment = Payment.builder()
                    .bookingId(booking.getId())
                    .stripePaymentIntentId(intent.getId())
                    .amount(booking.getTotalAmount())
                    .build(); // status defaults to PENDING until the webhook confirms it

            paymentRepository.save(payment);
            return new PaymentIntentResponse(payment.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            log.error("Stripe PaymentIntent creation failed for booking {}", bookingId, e);
            throw new BadRequestException("Could not initiate payment: " + e.getMessage());
        }
    }

    /**
     * Source of truth for payment completion. Never mark a booking CONFIRMED based on a
     * frontend call alone — only this webhook-driven path, after Stripe's signature has
     * been verified in the controller, is trusted to flip a booking's status.
     */
    @Transactional
    public void handleWebhookEvent(Event event) {
        if (!(event.getDataObjectDeserializer().getObject().orElse(null) instanceof StripeObject stripeObject)) {
            log.warn("Unhandled Stripe event with no deserializable object: {}", event.getType());
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> markPayment((PaymentIntent) stripeObject, PaymentStatus.SUCCEEDED, BookingStatus.CONFIRMED);
            case "payment_intent.payment_failed" -> markPayment((PaymentIntent) stripeObject, PaymentStatus.FAILED, null);
            default -> log.info("Ignoring unhandled Stripe event type: {}", event.getType());
        }
    }

    private void markPayment(PaymentIntent intent, PaymentStatus paymentStatus, BookingStatus bookingStatus) {
        Optional<Payment> maybePayment = paymentRepository.findByStripePaymentIntentId(intent.getId());
        if (maybePayment.isEmpty()) {
            log.warn(" received stripe event for unknown PaymentIntent {}", intent.getId());
            return;
        }

        Payment payment = maybePayment.get();
        payment.setStatus(paymentStatus);
        paymentRepository.save(payment);

        if (bookingStatus != null) {
            bookingRepository.findById(payment.getBookingId()).ifPresent(booking -> {
                booking.setStatus(bookingStatus);
                bookingRepository.save(booking);
            });
        }
    }
}
