package com.loconet.backend.service;

import com.loconet.backend.dto.UserDTO;
import com.loconet.backend.dto.UserRegistrationRequest;
import com.loconet.backend.exception.DuplicateResourceException;
import com.loconet.backend.exception.ResourceNotFoundException;
import com.loconet.backend.entity.Society;
import com.loconet.backend.entity.User;
import com.loconet.backend.entity.UserSociety;
import com.loconet.backend.entity.UserSocietyId;
import com.loconet.backend.repository.SocietyRepository;
import com.loconet.backend.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate; // Add kiya hua import
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;      // Add kiya hua import
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    // SRID 4326 = WGS84, matching the geography(...,4326) columns.
    private static final int SRID_WGS84 = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private final UserRepository userRepository;
    private final SocietyRepository societyRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       SocietyRepository societyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.societyRepository = societyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user, hashes their password, and attempts to assign
     * them a default Society (geo-matched first, then a fallback).
     */
    @Transactional
    public UserDTO registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with email " + request.getEmail() + " already exists");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("An account with phone number " + request.getPhoneNumber() + " already exists");
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

        Society assignedSociety = assignDefaultSociety(savedUser).orElse(null);
        if (assignedSociety != null) {
            attachToSociety(savedUser, assignedSociety);
        } else {
            log.warn("No society available to assign to newly registered user {}", savedUser.getId());
        }

        return toDto(savedUser, assignedSociety);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return toDto(user, null);
    }

    /**
     * Geo-matches the user's home location against society boundaries;
     * falls back to the earliest-created society (see SocietyRepository) if
     * no coordinates were supplied or none of the boundaries contain the point.
     */
    private Optional<Society> assignDefaultSociety(User user) {
        if (user.getHomeLocation() != null) {
            Optional<Society> matched = societyRepository.findSocietyContainingPoint(user.getHomeLocation());
            if (matched.isPresent()) {
                return matched;
            }
        }
        return societyRepository.findTopByOrderByCreatedAtAsc();
    }

    private void attachToSociety(User user, Society society) {
        UserSociety membership = UserSociety.builder()
                .id(new UserSocietyId(user.getId(), society.getId()))
                .user(user)
                .society(society)
                // No document is collected at registration time; membership
                // starts PENDING and gets a real document_url on verification.
                .documentUrl("PENDING_UPLOAD")
                .verificationStatus("PENDING")
                .build();
        user.getUserSocieties().add(membership);
        userRepository.save(user);
    }

    private Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        // JTS coordinates are (x, y) = (longitude, latitude).
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(SRID_WGS84);
        return point;
    }

    private UserDTO toDto(User user, Society society) {
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
                .defaultSocietyId(society != null ? society.getId() : null)
                .defaultSocietyName(society != null ? society.getName() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}