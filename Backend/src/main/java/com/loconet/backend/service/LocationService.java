// File: Backend/src/main/java/com/loconet/backend/service/LocationService.java
package com.loconet.backend.service;

import com.loconet.backend.dto.NearbyUserResponse;
import com.loconet.backend.entity.UserIntent;
import com.loconet.backend.dto.NearbyUserProjection;
import com.loconet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final UserRepository userRepository;

    public LocationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds users within radiusMeters of (latitude, longitude), excluding
     * currentUserId. The exclusion also happens inside the native query
     * (u.id <> :excludeUserId), so a requesting user is never in the result
     * set even before this mapping step — no post-filtering needed here.
     */
    @Transactional(readOnly = true)
    public List<NearbyUserResponse> findNearbyUsers(UUID currentUserId,
                                                      double latitude,
                                                      double longitude,
                                                      double radiusMeters) {
        List<NearbyUserProjection> rows =
                userRepository.findNearbyUsers(latitude, longitude, radiusMeters, currentUserId);

        return rows.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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
