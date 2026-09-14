// File: Backend/src/main/java/com/loconet/backend/exception/ConnectionConflictException.java
package com.loconet.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Covers two distinct but both-409 situations: a connection already exists
 * between the two users, or a request has already been responded to. Kept
 * as one exception type (rather than two) since callers only need the
 * status code + message, not to branch on which case occurred.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConnectionConflictException extends RuntimeException {

    public ConnectionConflictException(String message) {
        super(message);
    }
}
