// File: Backend/src/main/java/com/loconet/backend/repository/NearbyUserProjection.java
package com.loconet.backend.dto;

import java.util.UUID;

/**
 * Spring Data interface projection for UserRepository#findNearbyUsers.
 *
 * Not one of the four requested files, but needed to map the native query's
 * result columns to something typed without pulling JTS/GeometryFactory
 * into the service layer — getter names are matched to the SELECT aliases
 * (case-insensitively), so keep them in sync if you edit the query.
 *
 * intent/userTier come back as String here (enum cast to ::text in the
 * query) rather than the UserIntent enum directly, since native-query
 * projections map custom Postgres enum columns most reliably as text;
 * LocationService converts it to UserIntent via valueOf() when building
 * NearbyUserResponse.
 */
public interface NearbyUserProjection {

    UUID getId();

    String getFullName();

    String getIntent();

    String getUserTier();

    Double getLatitude();

    Double getLongitude();

    Double getDistanceInMeters();
}
