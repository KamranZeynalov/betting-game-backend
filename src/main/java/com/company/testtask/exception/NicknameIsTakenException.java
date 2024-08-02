package com.company.testtask.exception;

public class NicknameIsTakenException extends RuntimeException {

    public NicknameIsTakenException(String message) {
        super(message);
    }
}
