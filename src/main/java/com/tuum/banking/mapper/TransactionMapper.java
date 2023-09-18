package com.tuum.banking.mapper;

import com.tuum.banking.api.model.CreateTransactionResponse;
import com.tuum.banking.api.model.GetTransactionResponse;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;
import com.tuum.banking.query.repository.dto.TransactionDto;

public class TransactionMapper {

    public static CreateTransactionResponse mapAggregateToResponse(AccountAggregate aggregate) {
        var transaction = aggregate.getLastTransaction();

        return new CreateTransactionResponse(
                aggregate.getId(),
                transaction.getTransactionId(),
                transaction.getCurrency(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                aggregate.getBalances().get(transaction.getCurrency())
        );
    }

    public static TransactionDto mapEventToDto(TransactionEvent event) {
        return new TransactionDto(
                event.getTransactionId(),
                event.getAggregateId(),
                event.getType().name(),
                event.getCurrency().name(),
                event.getAmount(),
                event.getDescription()
        );
    }

    public static GetTransactionResponse mapDtoToResponse(TransactionDto dto) {
        return new GetTransactionResponse(
                dto.accountId(),
                dto.id(),
                Currency.valueOf(dto.currency()),
                dto.amount(),
                TransactionType.valueOf(dto.type()),
                dto.description()
        );
    }
}
