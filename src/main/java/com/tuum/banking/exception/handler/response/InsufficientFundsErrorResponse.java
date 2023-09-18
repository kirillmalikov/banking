package com.tuum.banking.exception.handler.response;

import com.tuum.banking.exception.InsufficientFundsException;
import lombok.Getter;

@Getter
public class InsufficientFundsErrorResponse {
    private final String account;
    private final String message;

    public InsufficientFundsErrorResponse(InsufficientFundsException e) {
        this.account = e.getAccount();
        this.message = e.getMessage();
    }
}
