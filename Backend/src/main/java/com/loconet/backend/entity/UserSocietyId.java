package com.loconet.backend.entity;

import com.loconet.backend.entity.UserSocietyId;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for user_societies (user_id, society_id).
 * Used with @EmbeddedId + @MapsId on the UserSociety entity.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSocietyId implements Serializable {

    private UUID userId;

    private UUID societyId;
}
