package com.rentalapp.property;

import com.rentalapp.common.exception.ForbiddenException;
import com.rentalapp.common.exception.ResourceNotFoundException;
import com.rentalapp.property.dto.PropertyRequest;
import com.rentalapp.property.dto.PropertyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyService propertyService;

    private UUID ownerId;
    private UUID propertyId;
    private Property property;
    private PropertyRequest request;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        property = Property.builder().id(propertyId).ownerId(ownerId).title("Old Title")
                .address("1 St").pricePerMonth(new BigDecimal("500")).build();
        request = new PropertyRequest("New Title", "desc", "1 St", "Cairo", new BigDecimal("600"), 2, 1);
    }

    @Test
    void ownerCanUpdateTheirOwnProperty() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        PropertyResponse response = propertyService.update(ownerId, propertyId, request);

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.pricePerMonth()).isEqualByComparingTo("600");
    }

    @Test
    void differentOwnerCannotUpdateProperty() {
        UUID intruderId = UUID.randomUUID();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> propertyService.update(intruderId, propertyId, request))
                .isInstanceOf(ForbiddenException.class);

        verify(propertyRepository, never()).save(any());
    }

    @Test
    void differentOwnerCannotDeleteProperty() {
        UUID intruderId = UUID.randomUUID();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> propertyService.delete(intruderId, propertyId))
                .isInstanceOf(ForbiddenException.class);

        verify(propertyRepository, never()).delete(any());
    }

    @Test
    void throwsNotFoundForMissingProperty() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.getById(propertyId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
