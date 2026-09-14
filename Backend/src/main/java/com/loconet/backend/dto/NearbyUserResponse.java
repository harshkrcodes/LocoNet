// File: Backend/src/main/java/com/loconet/backend/dto/NearbyUserResponse.java
package com.loconet.backend.dto;

import com.loconet.backend.entity.UserIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * API-facing result row for GET /api/location/nearby.
 * Assumes UserIntent lives at com.loconet.backend.model.enums.UserIntent,
 * mirroring the Phase 3 entity layout under the new base package — adjust
 * the import if your actual path differs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyUserResponse {

    private UUID id;
    private String fullName;
    private UserIntent intent;
    private String userTier;
    private Double latitude;
    private Double longitude;
    private Double distanceInMeters;
}
