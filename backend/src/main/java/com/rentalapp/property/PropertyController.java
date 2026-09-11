package com.rentalapp.property;

import com.rentalapp.property.dto.PropertyRequest;
import com.rentalapp.property.dto.PropertyResponse;
import com.rentalapp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> create(
            @AuthenticationPrincipal User owner,
            @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.create(owner.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<PropertyResponse> update(
            @AuthenticationPrincipal User owner,
            @PathVariable UUID id,
            @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.update(owner.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User owner, @PathVariable UUID id) {
        propertyService.delete(owner.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<PropertyResponse>> myProperties(@AuthenticationPrincipal User owner) {
        return ResponseEntity.ok(propertyService.getByOwner(owner.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PropertyResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.search(city, minPrice, maxPrice, pageable));
    }
}
