package com.rentalapp.property;

import com.rentalapp.common.exception.ForbiddenException;
import com.rentalapp.common.exception.ResourceNotFoundException;
import com.rentalapp.property.dto.PropertyRequest;
import com.rentalapp.property.dto.PropertyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional
    public PropertyResponse create(UUID ownerId, PropertyRequest request) {
        Property property = Property.builder()
                .ownerId(ownerId)
                .title(request.title())
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .pricePerMonth(request.pricePerMonth())
                .bedrooms(request.bedrooms())
                .bathrooms(request.bathrooms())
                .build();

        return PropertyResponse.from(propertyRepository.save(property));
    }

    @Transactional
    public PropertyResponse update(UUID ownerId, UUID propertyId, PropertyRequest request) {
        Property property = getOwnedProperty(ownerId, propertyId);

        property.setTitle(request.title());
        property.setDescription(request.description());
        property.setAddress(request.address());
        property.setCity(request.city());
        property.setPricePerMonth(request.pricePerMonth());
        property.setBedrooms(request.bedrooms());
        property.setBathrooms(request.bathrooms());

        return PropertyResponse.from(propertyRepository.save(property));
    }

    @Transactional
    public void delete(UUID ownerId, UUID propertyId) {
        Property property = getOwnedProperty(ownerId, propertyId);
        propertyRepository.delete(property);
    }

    public PropertyResponse getById(UUID propertyId) {
        return PropertyResponse.from(findOrThrow(propertyId));
    }

    public List<PropertyResponse> getByOwner(UUID ownerId) {
        return propertyRepository.findByOwnerId(ownerId).stream()
                .map(PropertyResponse::from)
                .toList();
    }

    public Page<PropertyResponse> search(String city, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return propertyRepository.search(city, minPrice, maxPrice, pageable)
                .map(PropertyResponse::from);
    }

    /** Loads a property and verifies the given owner actually owns it — never trust the client's word on this. */
    private Property getOwnedProperty(UUID ownerId, UUID propertyId) {
        Property property = findOrThrow(propertyId);
        if (!property.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("You do not own this property");
        }
        return property;
    }

    private Property findOrThrow(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
    }
}
