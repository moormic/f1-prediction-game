package com.moormic.f1.game.exception;

public class ApiException extends RuntimeException {

    public ApiException() {

    }

    public ApiException(String errorMessage) {
        super(errorMessage);
    }

}