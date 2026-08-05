package com.munchy.account.exception;

public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException(String message) {
        super(message);
    }
}
