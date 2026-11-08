package com.loconet.backend.service;

import com.loconet.backend.dto.ShoutRequest;
import com.loconet.backend.dto.ShoutResponse;
import com.loconet.backend.entity.Shout;
import com.loconet.backend.entity.User;
import com.loconet.backend.repository.ShoutRepository;
import com.loconet.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoutService {

    private final ShoutRepository shoutRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new hyperlocal shout and maps the geographic coordinates to a PostGIS Point.
     */
    @Transactional
    public ShoutResponse createShout(String principalName, ShoutRequest request) {
        User user = userRepository.findById(UUID.fromString(principalName))
                .orElseThrow(() -> new RuntimeException("User not found"));

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

        int radius = (request.getRadiusMeters() != null) ? request.getRadiusMeters() : 5000;

        Shout shout = Shout.builder()
                .user(user)
                .content(request.getContent())
                .originLocation(location)
                .radiusMeters(radius)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        shout = shoutRepository.save(shout);
        return mapToResponse(shout);
    }

    /**
     * Retrieves active shouts within the radius of the provided coordinates.
     * Transactional annotation ensures lazy-loaded properties are accessible.
     */
    @Transactional(readOnly = true)
    public List<ShoutResponse> getNearbyShouts(double latitude, double longitude) {
        return shoutRepository.findActiveShoutsNearby(latitude, longitude)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ShoutResponse mapToResponse(Shout shout) {
        return ShoutResponse.builder()
                .id(shout.getId())
                .authorId(shout.getUser().getId())
                .authorName(shout.getUser().getFullName())
                .content(shout.getContent())
                .createdAt(shout.getCreatedAt())
                .build();
    }
}