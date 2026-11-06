// File: Backend/src/main/java/com/loconet/backend/service/SocietyMembershipService.java
package com.loconet.backend.service;

import com.loconet.backend.exception.NotSocietyMemberException;
import com.loconet.backend.repository.UserSocietyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Single source of truth for "is this user a verified member of this
 * society?". Used by ChatService (post/read) and StompAuthChannelInterceptor
 * (SUBSCRIBE), so the two paths can never drift apart.
 */
@Service
public class SocietyMembershipService {

    /**
     * Nothing in this codebase moves a membership out of "PENDING" yet
     * (registration writes PENDING; there is no verification endpoint), so
     * until one exists nobody passes this check except via a manual
     * UPDATE user_societies SET verification_status = 'VERIFIED' ...
     */
    public static final String VERIFIED_STATUS = "VERIFIED";

    private final UserSocietyRepository userSocietyRepository;

    public SocietyMembershipService(UserSocietyRepository userSocietyRepository) {
        this.userSocietyRepository = userSocietyRepository;
    }

    @Transactional(readOnly = true)
    public boolean isVerifiedMember(UUID userId, UUID societyId) {
        return userSocietyRepository.existsByUser_IdAndSociety_IdAndVerificationStatus(
                userId, societyId, VERIFIED_STATUS);
    }

    /** Throws instead of returning a boolean so callers can't forget to act on the result. */
    @Transactional(readOnly = true)
    public void requireVerifiedMember(UUID userId, UUID societyId) {
        if (!isVerifiedMember(userId, societyId)) {
            throw new NotSocietyMemberException(
                    "User " + userId + " is not a verified member of society " + societyId);
        }
    }
}
