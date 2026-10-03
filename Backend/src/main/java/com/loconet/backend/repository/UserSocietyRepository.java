// File: Backend/src/main/java/com/loconet/backend/repository/UserSocietyRepository.java
//
// No UserSociety repository existed before this — the join entity was
// only ever written to (UserService.attachToSociety in Phase 3's
// onboarding flow), never queried. This is new, needed to back the
// society membership check.
//
// ASSUMPTION FLAGGED: I don't have your actual UserSociety.java /
// UserSocietyId.java under com.loconet.backend.entity (they were never
// regenerated for this package after the Phase 4 rename — same gap as
// UserRepository earlier). This assumes the Phase 3 field names (user,
// society as @ManyToOne associations, verificationStatus as a String) and
// guesses UserSocietyId lives at com.loconet.backend.entity.id — fix the
// import below if that's wrong.
package com.loconet.backend.repository;

//import com.loconet.backend.entity.UserSociety;
//import com.loconet.backend.entity.id.UserSocietyId;
import com.loconet.backend.entity.UserSociety;
import com.loconet.backend.entity.UserSocietyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSocietyRepository extends JpaRepository<UserSociety, UserSocietyId> {

    /**
     * Derived query resolves "User_Id"/"Society_Id" via the user/society
     * @ManyToOne associations' id fields (standard Spring Data nested
     * property traversal, same pattern as UserConnectionRepository).
     */
    boolean existsByUser_IdAndSociety_IdAndVerificationStatus(
            UUID userId, UUID societyId, String verificationStatus);
}
