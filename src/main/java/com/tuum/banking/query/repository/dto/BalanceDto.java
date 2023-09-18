package com.tuum.banking.query.repository.dto;

import com.tuum.banking.domain.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceDto(UUID accountId, Currency currency, BigDecimal amount) {}
