package com.tuum.banking.mapper;

import com.tuum.banking.api.model.CreateAccountResponse;
import com.tuum.banking.api.model.GetAccountResponse;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.tuum.banking.domain.Currency.EUR;
import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    @Test
    void mapAggregateToResponse() {
        var aggregate = new AccountAggregate(UUID.randomUUID(), UUID.randomUUID(), "EE");
        aggregate.getBalances().put(EUR, BigDecimal.TEN);

        var result = AccountMapper.mapAggregateToResponse(aggregate);

        assertThat(result)
                .extracting(CreateAccountResponse::accountId,
                            CreateAccountResponse::customerId,
                            CreateAccountResponse::balances
                )
                .containsExactly(aggregate.getId(),
                                 aggregate.getCustomerId(),
                                 List.of(new Balance(EUR, BigDecimal.TEN))
                );
    }

    @Test
    void mapCreatedEventToDto() {
        var event = new AccountCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "EE", Set.of(EUR));

        var result = AccountMapper.mapCreatedEventToDto(event);

        assertThat(result)
                .extracting(AccountDto::getId,
                            AccountDto::getCustomerId,
                            AccountDto::getCountry,
                            AccountDto::getBalances
                )
                .containsExactly(event.getAggregateId(),
                                 event.getCustomerId(),
                                 event.getCountry(),
                                 Set.of(new BalanceDto(event.getAggregateId(), EUR, BigDecimal.ZERO))
                );
    }

    @Test
    void mapDtoToResponse() {
        var dto = new AccountDto(UUID.randomUUID(), UUID.randomUUID(), "EE");
        dto.getBalances().add(new BalanceDto(dto.getId(), EUR, BigDecimal.ONE));

        var result = AccountMapper.mapDtoToResponse(dto);

        assertThat(result)
                .extracting(GetAccountResponse::accountId, GetAccountResponse::customerId)
                .containsExactly(dto.getId(), dto.getCustomerId());
        assertThat(result.balances())
                .extracting(Balance::currency, Balance::amount)
                .containsExactly(Tuple.tuple(EUR, BigDecimal.ONE.setScale(2)));
    }
}