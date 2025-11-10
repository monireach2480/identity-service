package com.digitalecosystem.identityservice.exception;

public class IdentityExistsException extends RuntimeException {
    public IdentityExistsException(String message) {
        super(message);
    }
}