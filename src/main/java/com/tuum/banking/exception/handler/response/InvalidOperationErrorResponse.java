package com.tuum.banking.exception.handler.response;

import com.tuum.banking.exception.InvalidOperationException;
import lombok.Getter;

@Getter
public class InvalidOperationErrorResponse {
    private final String reason;
    private final String message;

    public InvalidOperationErrorResponse(InvalidOperationException e) {
        this.reason = e.getReason();
        this.message = e.getMessage();
    }
}
