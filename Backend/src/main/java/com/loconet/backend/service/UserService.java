package com.loconet.backend.service;

import com.loconet.backend.dto.UserDTO;
import com.loconet.backend.dto.UserRegistrationRequest;
import com.loconet.backend.exception.DuplicateResourceException;
import com.loconet.backend.exception.ResourceNotFoundException;
import com.loconet.backend.entity.User;
import com.loconet.backend.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private static final int SRID_WGS84 = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .hometown(request.getHometown())
                .state(request.getState())
                .organization(request.getOrganization())
                .intent(request.getIntent())
                .interests(request.getInterests())
                .homeLocation(toPoint(request.getLatitude(), request.getLongitude()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("Naya user ban gaya successfully: {}", savedUser.getEmail());

        return toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return toDto(user);
    }

    private Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID_WGS84);
        return point;
    }

    private UserDTO toDto(User user) {
        Double lat = user.getHomeLocation() != null ? user.getHomeLocation().getY() : null;
        Double lng = user.getHomeLocation() != null ? user.getHomeLocation().getX() : null;

        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .hometown(user.getHometown())
                .state(user.getState())
                .organization(user.getOrganization())
                .intent(user.getIntent())
                .interests(user.getInterests())
                .userTier(user.getUserTier())
                .latitude(lat)
                .longitude(lng)
                .createdAt(user.getCreatedAt())
                .build();
    }
}