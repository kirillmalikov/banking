package com.tuum.banking.api.model;

import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull
        UUID accountId,
        @NotNull
        Currency currency,
        @NotNull
        @Positive
        @Digits(integer = 15, fraction = 2)
        BigDecimal amount,
        @NotNull
        TransactionType direction,
        @NotBlank
        String description
) {}
