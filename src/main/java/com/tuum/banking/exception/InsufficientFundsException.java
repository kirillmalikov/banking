package com.tuum.banking.exception;

import lombok.Getter;

@Getter
public class InsufficientFundsException extends TuumException {

    private final String account;

    public InsufficientFundsException(String account) {
        super(String.format("insufficient funds on account %s", account));
        this.account = account;
    }
}
