package com.resumescreening.exception;

public class InterviewSessionNotFoundException extends RuntimeException {

    public InterviewSessionNotFoundException(String message) {
        super(message);
    }
}
