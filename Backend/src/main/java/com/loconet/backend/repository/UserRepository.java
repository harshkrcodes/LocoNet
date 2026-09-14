package com.loconet.backend.repository;

import com.loconet.backend.entity.User;
import com.loconet.backend.dto.NearbyUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Finds users within radiusMeters of (lat, lon), excluding excludeUserId,
     * ordered nearest-first. Builds the search point directly in SQL via
     * ST_SetSRID(ST_MakePoint(...), 4326)::geography.
     */
    @Query(value = """
            SELECT
                u.id AS id,
                u.full_name AS fullName,
                u.intent::text AS intent,
                u.user_tier AS userTier,
                ST_Y(u.home_location::geometry) AS latitude,
                ST_X(u.home_location::geometry) AS longitude,
                ST_Distance(
                    u.home_location,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
                ) AS distanceInMeters
            FROM users u
            WHERE u.home_location IS NOT NULL
              AND u.id <> :excludeUserId
              AND ST_DWithin(
                    u.home_location,
                    ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                    :radiusMeters
                  )
            ORDER BY distanceInMeters ASC
            """, nativeQuery = true)
    List<NearbyUserProjection> findNearbyUsers(
            @Param("lat") double latitude,
            @Param("lon") double longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("excludeUserId") UUID excludeUserId
    );
}