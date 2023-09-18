package com.tuum.banking.es.events;

import com.tuum.banking.domain.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AccountCreatedEvent extends BaseEvent {

    private final UUID customerId;
    private final String country;
    private final Set<Currency> currencies;

    public AccountCreatedEvent(UUID aggregateId, UUID customerId, String country, Set<Currency> currencies) {
        super(aggregateId);
        this.customerId = customerId;
        this.country = country;
        this.currencies = currencies;
    }
}

