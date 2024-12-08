package org.example.journal_app.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException{

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
