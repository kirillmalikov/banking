package com.tuum.banking.exception;

import lombok.Getter;

@Getter
public class InvalidOperationException extends TuumException {

    private final String reason;
    private final String message;

    public InvalidOperationException(String reason, String message) {
        super(message);
        this.reason = reason;
        this.message = message;
    }
}
