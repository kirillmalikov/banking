package com.tuum.banking.query.repository.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDto(
        UUID id,
        UUID accountId,
        String type,
        String currency,
        BigDecimal amount,
        String description
) {}
