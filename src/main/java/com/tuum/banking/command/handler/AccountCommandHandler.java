package com.tuum.banking.command.handler;

import com.tuum.banking.command.CreateAccountCommand;
import com.tuum.banking.command.TransactionCommand;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.es.EventSourcingHandler;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.es.events.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountCommandHandler implements CommandHandler<AccountAggregate> {

    private final EventSourcingHandler<AccountAggregate> eventSourcingHandler;

    @Override
    @Transactional(value = "eventStoreTransactionManager")
    public AccountAggregate handle(CreateAccountCommand command) {
        var aggregate = new AccountAggregate(UUID.randomUUID());
        var event = new AccountCreatedEvent(
                aggregate.getId(),
                command.customerId(),
                command.country(),
                command.currencies()
        );
        applyAndSave(aggregate, event);

        return aggregate;
    }

    @Override
    @Transactional(value = "eventStoreTransactionManager")
    public AccountAggregate handle(TransactionCommand command) {
        var aggregate = eventSourcingHandler.recreateAggregate(command.accountId());
        var event = new TransactionEvent(
                aggregate.getId(),
                UUID.randomUUID(),
                command.currency(),
                command.amount(),
                command.type(),
                command.description()
        );
        applyAndSave(aggregate, event);

        return aggregate;
    }

    private void applyAndSave(AccountAggregate aggregate, BaseEvent event) {
        aggregate.apply(event);
        eventSourcingHandler.save(aggregate);
    }
}
