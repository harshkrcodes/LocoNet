package com.loconet.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface InboxProjection {
    UUID getUserId();
    String getFullName();
    String getLastMessage();
    LocalDateTime getTimestamp();
    int getUnreadCount();
}
