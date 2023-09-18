package com.tuum.banking.command.handler;

import com.tuum.banking.command.CreateAccountCommand;
import com.tuum.banking.command.TransactionCommand;
import com.tuum.banking.domain.AggregateRoot;

public interface CommandHandler<A extends AggregateRoot> {

    A handle(CreateAccountCommand command);

    A handle(TransactionCommand command);
}
