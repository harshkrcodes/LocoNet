package com.loconet.backend.repository;

import com.loconet.backend.entity.Society;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SocietyRepository extends JpaRepository<Society, UUID> {

    /**
     * Finds the society whose polygon boundary covers the given point.
     * Uses PostGIS ST_Covers directly against the geography(Polygon,4326)
     * column, so no manual geometry casting is needed.
     */
    @Query(value = "SELECT * FROM societies s " +
            "WHERE s.boundary IS NOT NULL AND ST_Covers(s.boundary, :point) " +
            "LIMIT 1", nativeQuery = true)
    Optional<Society> findSocietyContainingPoint(@Param("point") Point point);

    /**
     * Fallback "default" society for when a user's point can't be matched to
     * any boundary (missing coordinates, or genuinely outside every known
     * society). The schema has no `is_default` flag, so this uses the
     * earliest-created society as a stand-in — swap for a real flag/column
     * if you need a curated default instead.
     */
    Optional<Society> findTopByOrderByCreatedAtAsc();
}
