// File: Backend/src/main/java/com/loconet/backend/dto/LoginResponseDTO.java
package com.loconet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private UUID userId;
    private String email;
}
