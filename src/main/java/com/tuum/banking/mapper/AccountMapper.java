package com.tuum.banking.mapper;

import com.tuum.banking.api.model.CreateAccountResponse;
import com.tuum.banking.api.model.GetAccountResponse;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;

import java.math.BigDecimal;

public class AccountMapper {

    public static CreateAccountResponse mapAggregateToResponse(AccountAggregate aggregate) {
        return new CreateAccountResponse(aggregate.getId(),
                                         aggregate.getCustomerId(),
                                         aggregate.getBalances()
                                                 .entrySet()
                                                 .stream()
                                                 .map(entry -> new Balance(entry.getKey(), entry.getValue()))
                                                 .toList()
        );
    }

    public static AccountDto mapCreatedEventToDto(AccountCreatedEvent event) {
        var account = new AccountDto(event.getAggregateId(), event.getCustomerId(), event.getCountry());
        account.getBalances()
                .addAll(
                        event.getCurrencies()
                                .stream()
                                .map(currency -> new BalanceDto(event.getAggregateId(), currency, BigDecimal.ZERO))
                                .toList()
                );

        return account;
    }

    public static GetAccountResponse mapDtoToResponse(AccountDto accountDto) {
        return new GetAccountResponse(
                accountDto.getId(),
                accountDto.getCustomerId(),
                accountDto.getBalances().stream().map(AccountMapper::mapBalanceDto).toList()
        );
    }

    private static Balance mapBalanceDto(BalanceDto balanceDto) {
        return new Balance(balanceDto.currency(), balanceDto.amount().setScale(2));
    }
}
