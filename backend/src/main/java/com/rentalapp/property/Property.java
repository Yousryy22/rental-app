package com.rentalapp.property;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    private String city;

    @Column(name = "price_per_month", nullable = false)
    private BigDecimal pricePerMonth;

    private Integer bedrooms;
    private Integer bathrooms;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // Images are a separate entity (property_images table), fetched via
    // PropertyImageRepository.findByPropertyId — kept out of this entity
    // to avoid a bidirectional mapping on a plain UUID FK column.
}
