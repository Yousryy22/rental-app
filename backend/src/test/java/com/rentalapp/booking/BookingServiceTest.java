package com.rentalapp.booking;

import com.rentalapp.booking.dto.BookingRequest;
import com.rentalapp.booking.dto.BookingResponse;
import com.rentalapp.common.exception.BadRequestException;
import com.rentalapp.common.exception.ForbiddenException;
import com.rentalapp.common.exception.ResourceNotFoundException;
import com.rentalapp.property.Property;
import com.rentalapp.property.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private BookingService bookingService;

    private UUID clientId;
    private UUID propertyId;
    private Property property;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        property = Property.builder()
                .id(propertyId)
                .ownerId(UUID.randomUUID())
                .title("Cozy Loft")
                .address("123 Main St")
                .pricePerMonth(new BigDecimal("900.00"))
                .build();
    }

    @Test
    void createsBookingAndComputesProRatedTotal() {
        BookingRequest request = new BookingRequest(propertyId, LocalDate.now().plusDays(1), LocalDate.now().plusDays(31));

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(bookingRepository.findOverlapping(eq(propertyId), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.create(clientId, request);

        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo("900.00"); // 30 days == 1 month at 900/mo
    }

    @Test
    void rejectsOverlappingBooking() {
        BookingRequest request = new BookingRequest(propertyId, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(bookingRepository.findOverlapping(eq(propertyId), any(), any()))
                .thenReturn(List.of(Booking.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> bookingService.create(clientId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not available");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectsEndDateNotAfterStartDate() {
        LocalDate date = LocalDate.now().plusDays(3);
        BookingRequest request = new BookingRequest(propertyId, date, date);

        assertThatThrownBy(() -> bookingService.create(clientId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date must be after start date");

        verifyNoInteractions(propertyRepository);
    }

    @Test
    void throwsWhenPropertyDoesNotExist() {
        BookingRequest request = new BookingRequest(propertyId, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(clientId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void onlyOwningClientCanCancelBooking() {
        UUID bookingId = UUID.randomUUID();
        UUID otherClientId = UUID.randomUUID();
        Booking booking = Booking.builder().id(bookingId).clientId(otherClientId).status(BookingStatus.PENDING).build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(clientId, bookingId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cancelSucceedsForOwningClient() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = Booking.builder().id(bookingId).clientId(clientId).status(BookingStatus.PENDING).build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancel(clientId, bookingId);

        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
    }
}
