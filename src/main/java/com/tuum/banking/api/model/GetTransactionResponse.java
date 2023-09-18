package com.tuum.banking.api.model;

import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record GetTransactionResponse(
        UUID accountId,
        UUID transactionId,
        Currency currency,
        BigDecimal amount,
        TransactionType direction,
        String description
) {}
