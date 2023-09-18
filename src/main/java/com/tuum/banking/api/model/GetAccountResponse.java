package com.tuum.banking.api.model;

import com.tuum.banking.domain.Balance;

import java.util.List;
import java.util.UUID;

public record GetAccountResponse(UUID accountId, UUID customerId, List<Balance> balances) {}
