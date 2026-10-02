package com.rentalapp.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByClientId(UUID clientId);

    List<Booking> findByPropertyId(UUID propertyId);

    /** Any CONFIRMED or PENDING booking on the same property whose range overlaps the requested one. */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.propertyId = :propertyId
              AND b.status IN ('PENDING', 'CONFIRMED')
              AND b.startDate < :endDate
              AND b.endDate > :startDate
            """)
    List<Booking> findOverlapping(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
