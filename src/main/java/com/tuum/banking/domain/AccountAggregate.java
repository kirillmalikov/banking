package com.tuum.banking.domain;

import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;
import com.tuum.banking.exception.InsufficientFundsException;
import com.tuum.banking.exception.InvalidOperationException;
import com.tuum.banking.exception.TuumException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@AllArgsConstructor
public class AccountAggregate extends AggregateRoot<BaseEvent> {

    public AccountAggregate(UUID id) {
        super(id);
    }

    public AccountAggregate(UUID id, UUID customerId, String country) {
        super(id);
        this.customerId = customerId;
        this.country = country;
    }

    public AccountAggregate(UUID id, UUID customerId, String country, TransactionEvent lastTransaction) {
        super(id);
        this.customerId = customerId;
        this.country = country;
        this.lastTransaction = lastTransaction;
    }

    private UUID customerId;
    private String country;
    private final Map<Currency, BigDecimal> balances = new ConcurrentHashMap<>();
    private TransactionEvent lastTransaction;

    @Override
    protected void on(BaseEvent event) {

        if (event instanceof AccountCreatedEvent) {
            handle((AccountCreatedEvent) event);
        } else if (event instanceof TransactionEvent) {
            handle((TransactionEvent) event);
        } else {
            throw new TuumException(String.format("invalid event type %s", event.getClass().getSimpleName()));
        }
    }

    protected void handle(AccountCreatedEvent event) {
        this.customerId = event.getCustomerId();
        this.country = event.getCountry();
        event.getCurrencies().forEach(currency -> this.balances.put(currency, BigDecimal.ZERO));
    }

    protected void handle(TransactionEvent event) {
        var currency = event.getCurrency();
        var balance = this.balances.get(currency);

        if (balance == null) {
            throw new InvalidOperationException(
                    "invalid currency",
                    String.format("currency %s is not available for account %s", currency, this.getId())
            );
        }

        if (event.getType().equals(TransactionType.IN)) {
            this.balances.put(currency, balance.add(event.getAmount()));
        } else {
            if (balance.compareTo(event.getAmount()) < 0) {
                throw new InsufficientFundsException(this.getId().toString());
            }

            this.balances.put(currency, balance.subtract(event.getAmount()));
        }
        this.lastTransaction = event;
    }
}
