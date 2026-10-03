// File: Backend/src/main/java/com/loconet/backend/controller/LocationController.java
package com.loconet.backend.controller;

import com.loconet.backend.dto.NearbyUserResponse;
import com.loconet.backend.service.LocationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/location")
@Validated
public class LocationController {

    private static final double DEFAULT_RADIUS_METERS = 5000.0;
    private static final long MAX_RADIUS_METERS = 50_000L;

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * GET /api/location/nearby?lat=...&lon=...&radiusMeters=...
     *
     * UPDATED for Phase 6: userId is no longer a query param — it comes
     * from @AuthenticationPrincipal (populated by JwtAuthFilter from the
     * JWT), so a caller can only ever search "as themselves". This is a
     * breaking change to the endpoint's query parameters.
     *
     * Out-of-range lat/lon/radiusMeters values are rejected with 400 via
     * Spring's built-in method-validation handling (no custom exception
     * handler needed for that path).
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyUserResponse>> findNearbyUsers(
            @AuthenticationPrincipal UUID userId,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lon,
            @RequestParam(required = false) @Positive @Max(MAX_RADIUS_METERS) Double radiusMeters
    ) {
        double radius = radiusMeters != null ? radiusMeters : DEFAULT_RADIUS_METERS;
        List<NearbyUserResponse> results = locationService.findNearbyUsers(userId, lat, lon, radius);
        return ResponseEntity.ok(results);
    }
}
