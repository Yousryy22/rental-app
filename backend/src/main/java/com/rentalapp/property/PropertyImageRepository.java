package com.rentalapp.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {
    List<PropertyImage> findByPropertyId(UUID propertyId);
}
