// File: Backend/src/main/java/com/loconet/backend/exception/LocationNotSetException.java
package com.loconet.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown by GET /api/location/nearby when the caller has never posted a location. */
@ResponseStatus(HttpStatus.CONFLICT)
public class LocationNotSetException extends RuntimeException {

    public LocationNotSetException(String message) {
        super(message);
    }
}
