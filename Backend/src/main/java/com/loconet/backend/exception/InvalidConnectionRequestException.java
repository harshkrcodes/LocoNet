// File: Backend/src/main/java/com/loconet/backend/exception/InvalidConnectionRequestException.java
package com.loconet.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidConnectionRequestException extends RuntimeException {

    public InvalidConnectionRequestException(String message) {
        super(message);
    }
}
