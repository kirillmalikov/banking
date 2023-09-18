package com.tuum.banking.command;

import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCommand(
        UUID accountId,
        Currency currency,
        BigDecimal amount,
        TransactionType type,
        String description
) {}
