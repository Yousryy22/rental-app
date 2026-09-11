package com.rentalapp.property;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "property_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyImage {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(nullable = false)
    private String url;

    @Column(name = "is_primary")
    @Builder.Default
    private boolean primary = false;
}
