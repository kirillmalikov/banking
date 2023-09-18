package com.tuum.banking.mapper;

import com.tuum.banking.api.model.CreateTransactionResponse;
import com.tuum.banking.api.model.GetTransactionResponse;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;
import com.tuum.banking.query.repository.dto.TransactionDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.tuum.banking.domain.Currency.EUR;
import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    @Test
    void mapAggregateToResponse() {
        var accountId = UUID.randomUUID();
        var transaction = new TransactionEvent(
                accountId,
                UUID.randomUUID(),
                EUR,
                BigDecimal.TEN,
                TransactionType.IN,
                "test description"
        );
        var aggregate = new AccountAggregate(accountId, UUID.randomUUID(), "EE", transaction);
        aggregate.getBalances().put(EUR, BigDecimal.TEN);

        var result = TransactionMapper.mapAggregateToResponse(aggregate);

        assertThat(result)
                .extracting(CreateTransactionResponse::accountId,
                            CreateTransactionResponse::transactionId,
                            CreateTransactionResponse::currency,
                            CreateTransactionResponse::amount,
                            CreateTransactionResponse::type,
                            CreateTransactionResponse::description,
                            CreateTransactionResponse::balance
                )
                .containsExactly(aggregate.getId(),
                                 transaction.getTransactionId(),
                                 transaction.getCurrency(),
                                 transaction.getAmount(),
                                 transaction.getType(),
                                 transaction.getDescription(),
                                 aggregate.getBalances().get(EUR)
                );
    }

    @Test
    void mapEventToDto() {
        var event = new TransactionEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                EUR,
                BigDecimal.TEN,
                TransactionType.IN,
                "test description"
        );

        var result = TransactionMapper.mapEventToDto(event);

        assertThat(result)
                .extracting(TransactionDto::id,
                            TransactionDto::accountId,
                            TransactionDto::currency,
                            TransactionDto::amount,
                            TransactionDto::type,
                            TransactionDto::description
                )
                .containsExactly(event.getTransactionId(),
                                 event.getAggregateId(),
                                 event.getCurrency().name(),
                                 event.getAmount(),
                                 event.getType().name(),
                                 event.getDescription()
                );
    }

    @Test
    void mapDtoToResponse() {
        var dto = new TransactionDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IN",
                "EUR",
                BigDecimal.TEN,
                "test description"
        );

        var result = TransactionMapper.mapDtoToResponse(dto);

        assertThat(result)
                .extracting(GetTransactionResponse::accountId,
                            GetTransactionResponse::transactionId,
                            GetTransactionResponse::currency,
                            GetTransactionResponse::amount,
                            GetTransactionResponse::direction,
                            GetTransactionResponse::description
                )
                .containsExactly(dto.accountId(),
                                 dto.id(),
                                 Currency.valueOf(dto.currency()),
                                 dto.amount(),
                                 TransactionType.valueOf(dto.type()),
                                 dto.description()
                );
    }
}