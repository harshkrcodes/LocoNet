// File: Backend/src/main/java/com/loconet/backend/exception/NotSocietyMemberException.java
package com.loconet.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotSocietyMemberException extends RuntimeException {

    public NotSocietyMemberException(String message) {
        super(message);
    }
}
