// File: Backend/src/main/java/com/loconet/backend/service/LocationService.java
package com.loconet.backend.service;

import com.loconet.backend.dto.NearbyUserResponse;
import com.loconet.backend.entity.UserIntent;
import com.loconet.backend.exception.LocationNotSetException;
import com.loconet.backend.exception.ResourceNotFoundException;
import com.loconet.backend.repository.NearbyUserProjection;
import com.loconet.backend.repository.UserRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LocationService {

    private final UserRepository userRepository;

    public LocationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Single UPDATE statement (no select-then-save). Zero affected rows means
     * the JWT's userId has no row in users — surface that instead of
     * silently "succeeding".
     */
    @Transactional
    public void updateLocation(UUID userId, double latitude, double longitude) {
        int updated = userRepository.updateLocation(userId, latitude, longitude);
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
    }

    /**
     * The search origin is the caller's own stored location (written by
     * updateLocation), so clients never send — or spoof — the origin.
     */
    @Transactional(readOnly = true)
    public List<NearbyUserResponse> findNearbyUsers(UUID userId, double radiusMeters) {
        Point origin = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId))
                .getHomeLocation();

        if (origin == null) {
            throw new LocationNotSetException("Location not set — POST /api/location/update first");
        }

        // JTS Point: x = longitude, y = latitude.
        return userRepository.findNearbyUsers(origin.getY(), origin.getX(), radiusMeters, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private NearbyUserResponse toResponse(NearbyUserProjection row) {
        return NearbyUserResponse.builder()
                .id(row.getId())
                .fullName(row.getFullName())
                .intent(row.getIntent() != null ? UserIntent.valueOf(row.getIntent()) : null)
                .userTier(row.getUserTier())
                .latitude(row.getLatitude())
                .longitude(row.getLongitude())
                .distanceInMeters(row.getDistanceInMeters())
                .build();
    }
}
