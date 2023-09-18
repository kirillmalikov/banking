package com.tuum.banking.exception.handler.response;

import com.tuum.banking.exception.NotFoundException;
import lombok.Getter;

@Getter
public class NotFoundErrorResponse {

    private final String entity;
    private final String message;

    public NotFoundErrorResponse(NotFoundException e) {
        this.entity = e.getEntity();
        this.message = e.getMessage();
    }
}
