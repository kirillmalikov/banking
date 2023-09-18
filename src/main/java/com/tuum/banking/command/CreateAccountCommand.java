package com.tuum.banking.command;

import com.tuum.banking.domain.Currency;

import java.util.Set;
import java.util.UUID;

public record CreateAccountCommand(UUID customerId, String country, Set<Currency> currencies) {}
