package com.tuum.banking.query.repository.dto;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class AccountDto {

    private UUID id;
    private UUID customerId;
    private String country;
    private Set<BalanceDto> balances = new HashSet<>();

    public AccountDto(UUID id, UUID customerId, String country) {
        this.id = id;
        this.customerId = customerId;
        this.country = country;
    }
}
