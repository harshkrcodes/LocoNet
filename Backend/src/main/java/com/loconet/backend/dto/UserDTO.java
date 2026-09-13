package com.loconet.backend.dto;

import com.loconet.backend.entity.UserIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * API-facing representation of a User. Deliberately excludes passwordHash
 * and doesn't serialize the entity's lazy relationship collections directly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String hometown;
    private String state;
    private String organization;
    private UserIntent intent;
    private List<String> interests;
    private String userTier;

    // Flattened from the JTS Point so clients don't need a spatial library.
    private Double latitude;
    private Double longitude;

    private UUID defaultSocietyId;
    private String defaultSocietyName;

    private LocalDateTime createdAt;
}
