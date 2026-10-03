// File: Backend/src/main/java/com/loconet/backend/dto/ConnectionRequestDTO.java
// UPDATED for Phase 6 identity-trust pass — senderId REMOVED. It's no
// longer a client-supplied field; ConnectionController derives it from
// the authenticated Principal (@AuthenticationPrincipal) and passes it to
// ConnectionService.sendRequest() as a separate argument. This is a
// breaking change to the request body shape.
package com.loconet.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDTO {

    @NotNull
    private UUID receiverId;
}
