// File: Backend/src/main/java/com/loconet/backend/exception/UnauthorizedConnectionActionException.java
package com.loconet.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when the authenticated caller tries to respond to a connection
 * request that wasn't sent to them (i.e. they're not the receiver). Found
 * while closing the Phase 6 identity-trust pass — PUT /respond previously
 * had no check at all for this.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedConnectionActionException extends RuntimeException {

    public UnauthorizedConnectionActionException(String message) {
        super(message);
    }
}
