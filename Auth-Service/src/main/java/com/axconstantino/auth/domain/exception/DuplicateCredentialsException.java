package com.axconstantino.auth.domain.exception;

public class DuplicateCredentialsException extends IllegalArgumentException {

    public DuplicateCredentialsException(String message) {
        super(message);
    }
}
