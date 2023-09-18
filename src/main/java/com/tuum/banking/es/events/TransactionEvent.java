package com.tuum.banking.es.events;

import com.tuum.banking.domain.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor(force = true)
public class TransactionEvent extends BaseEvent {

    private final UUID transactionId;
    private final Currency currency;
    private final BigDecimal amount;
    private final TransactionType type;
    private final String description;

    public TransactionEvent(
            UUID aggregateId,
            UUID transactionId,
            Currency currency,
            BigDecimal amount,
            TransactionType type,
            String description
    ) {
        super(aggregateId);
        this.transactionId = transactionId;
        this.currency = currency;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public enum TransactionType {
        IN, OUT
    }
}
