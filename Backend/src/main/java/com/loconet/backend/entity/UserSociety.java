package com.loconet.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loconet.backend.entity.UserSocietyId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join entity for the many-to-many user_societies table. It carries extra
 * columns (document_url, verification_status, verified_by) so it is modeled
 * as its own entity with a composite @EmbeddedId rather than a plain
 * @ManyToMany.
 */
@Entity
@Table(name = "user_societies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSociety {

    @EmbeddedId
    private UserSocietyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("societyId")
    @JoinColumn(name = "society_id")
    @JsonIgnore
    @ToString.Exclude
    private Society society;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    @Column(name = "verification_status", length = 20)
    @Builder.Default
    private String verificationStatus = "PENDING";

    // Nullable FK to the admin user who verified this membership.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    @JsonIgnore
    @ToString.Exclude
    private User verifiedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
