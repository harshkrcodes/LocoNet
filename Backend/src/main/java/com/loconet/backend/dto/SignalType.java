// File: Backend/src/main/java/com/loconet/backend/dto/SignalType.java
//
// Placed in dto/, not entity/ — earlier phases' "enums live in entity"
// rule was for enums backing a persisted column (MatchStatus, MessageStatus).
// Signaling has no entity or table at all (see ChatController vs
// SignalingController comments), so there's nothing for this to belong to
// in entity/. Flagging the deviation since it breaks that prior pattern.
package com.loconet.backend.dto;

public enum SignalType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE
}
