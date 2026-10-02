package com.rentalapp.property.dto;

import com.rentalapp.property.Property;
import com.rentalapp.property.PropertyStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        String address,
        String city,
        BigDecimal pricePerMonth,
        Integer bedrooms,
        Integer bathrooms,
        PropertyStatus status
) {
    public static PropertyResponse from(Property p) {
        return new PropertyResponse(p.getId(), p.getOwnerId(), p.getTitle(), p.getDescription(),
                p.getAddress(), p.getCity(), p.getPricePerMonth(), p.getBedrooms(), p.getBathrooms(),
                p.getStatus());
    }
}
