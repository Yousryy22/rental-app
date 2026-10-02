package com.rentalapp.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {

    List<Property> findByOwnerId(UUID ownerId);

    @Query("""
            SELECT p FROM Property p
            WHERE p.status = 'AVAILABLE'
              AND (:city IS NULL OR LOWER(p.city) = LOWER(:city))
              AND (:minPrice IS NULL OR p.pricePerMonth >= :minPrice)
              AND (:maxPrice IS NULL OR p.pricePerMonth <= :maxPrice)
            """)
    Page<Property> search(
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
