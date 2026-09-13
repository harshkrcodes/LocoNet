package com.loconet.backend.dto;

import com.loconet.backend.entity.UserIntent;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Incoming payload for POST /api/users/register.
 * Kept separate from UserDTO so response shape (no password) never leaks
 * into the request contract, and vice versa.
 */
@Data
public class UserRegistrationRequest {

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    // 72 chars is BCrypt's effective input limit; longer is silently truncated.
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be 7-15 digits, optionally prefixed with +")
    private String phoneNumber;

    @Size(max = 100)
    private String hometown;

    @Size(max = 100)
    private String state;

    @Size(max = 150)
    private String organization;

    private UserIntent intent;

    private List<String> interests;

    // Optional home coordinates. If omitted, default-society assignment
    // falls back to the earliest-created society instead of geo-matching.
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;
}
