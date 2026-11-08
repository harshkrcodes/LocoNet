// File: src/main/java/com/loconet/backend/repository/ShoutRepository.java
package com.loconet.backend.repository;

import com.loconet.backend.entity.Shout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShoutRepository extends JpaRepository<Shout, UUID> {

    /**
     * Fetches active shouts within the specified radius using PostGIS spatial querying.
     * Utilizes ST_DWithin to calculate the geographic distance between the shout's origin
     * and the user's current coordinates. Filters out expired shouts.
     */
    @Query(value = "SELECT * FROM shouts s WHERE " +
            "ST_DWithin(s.origin_location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), s.radius_meters) = true " +
            "AND s.expires_at > CURRENT_TIMESTAMP " +
            "ORDER BY s.created_at DESC",
            nativeQuery = true)
    List<Shout> findActiveShoutsNearby(@Param("latitude") double latitude,
                                       @Param("longitude") double longitude);
}
