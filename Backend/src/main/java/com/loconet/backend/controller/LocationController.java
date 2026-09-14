// File: Backend/src/main/java/com/loconet/backend/controller/LocationController.java
package com.loconet.backend.controller;

import com.loconet.backend.dto.NearbyUserResponse;
import com.loconet.backend.service.LocationService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
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
     * GET /api/location/nearby?userId=...&lat=...&lon=...&radiusMeters=...
     *
     * `userId` is a query param rather than pulled from a security context
     * because no authentication layer exists yet in this codebase — swap it
     * for @AuthenticationPrincipal (or equivalent) once auth lands, and
     * drop the param.
     *
     * Out-of-range lat/lon/radiusMeters values are rejected with 400 via
     * Spring's built-in method-validation handling (no custom exception
     * handler needed for that path).
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyUserResponse>> findNearbyUsers(
            @RequestParam UUID userId,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double lon,
            @RequestParam(required = false) @Positive @Max(MAX_RADIUS_METERS) Double radiusMeters
    ) {
        double radius = radiusMeters != null ? radiusMeters : DEFAULT_RADIUS_METERS;
        List<NearbyUserResponse> results = locationService.findNearbyUsers(userId, lat, lon, radius);
        return ResponseEntity.ok(results);
    }
}
