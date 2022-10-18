package com.mateusz.library.exception.domain;

public class UsernameExistsException extends Exception{

    public UsernameExistsException(String message) {
        super(message);
    }
}
