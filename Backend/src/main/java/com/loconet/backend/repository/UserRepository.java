package com.loconet.backend.repository;

import com.loconet.backend.entity.User;
import com.loconet.backend.dto.NearbyUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // FIX 1: Location ke sath 'updated_at' ko current time par set kar diya
    @Modifying
    @Query(value = "UPDATE users SET home_location = ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), updated_at = CURRENT_TIMESTAMP WHERE id = :userId", nativeQuery = true)
    void updateUserLocation(@Param("userId") UUID userId, @Param("lat") double lat, @Param("lon") double lon);

    // FIX 2: AND u.updated_at >= NOW() - INTERVAL '30 minutes' add kiya
    @Query(value = """
        SELECT
            u.id AS id,
            u.full_name AS fullName,
            u.intent::text AS intent,
            u.user_tier AS userTier,
            ST_Y(u.home_location::geometry) AS latitude,
            ST_X(u.home_location::geometry) AS longitude,
            ST_Distance(
                u.home_location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
            ) AS distanceInMeters
        FROM users u
        WHERE u.id <> :excludeUserId
          AND u.home_location IS NOT NULL
          AND u.updated_at >= NOW() - INTERVAL '30 minutes'
          AND ST_DWithin(
                u.home_location::geography,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
          )
        ORDER BY distanceInMeters ASC
    """, nativeQuery = true)
    List<NearbyUserProjection> findNearbyUsers(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusMeters") double radiusMeters,
            @Param("excludeUserId") UUID excludeUserId
    );
}