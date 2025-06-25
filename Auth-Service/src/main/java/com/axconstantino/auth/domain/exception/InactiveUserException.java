package com.axconstantino.auth.domain.exception;

public class InactiveUserException extends RuntimeException{
    public InactiveUserException(String message) {
        super(message);
    }
}
