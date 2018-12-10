package com.girish.spellchecker.hash.exception;

public class InvalidHashException extends RuntimeException {
    public InvalidHashException(String message) {
        super(message);
    }

    public InvalidHashException(String message, Throwable cause) {
        super(message, cause);
    }
}
