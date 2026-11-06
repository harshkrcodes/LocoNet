// File: Backend/src/main/java/com/loconet/backend/exception/InvalidChatMessageException.java
package com.loconet.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Business-rule rejection of a chat message (self-message, empty, too long). */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidChatMessageException extends RuntimeException {

    public InvalidChatMessageException(String message) {
        super(message);
    }
}
