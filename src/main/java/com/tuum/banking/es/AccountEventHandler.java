package com.tuum.banking.es;

import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.exception.NotFoundException;
import com.tuum.banking.exception.TuumException;
import com.tuum.banking.query.repository.AccountQueryRepository;
import com.tuum.banking.query.repository.dto.BalanceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.tuum.banking.es.events.TransactionEvent.TransactionType.IN;
import static com.tuum.banking.mapper.AccountMapper.mapCreatedEventToDto;
import static com.tuum.banking.mapper.TransactionMapper.mapEventToDto;

@Component
@RequiredArgsConstructor
public class AccountEventHandler implements EventHandler {

    private final AccountQueryRepository queryRepository;

    @Override
    @Transactional(value = "readDbTransactionManager", propagation = Propagation.SUPPORTS)
    public void on(AccountCreatedEvent event) {
        var account = mapCreatedEventToDto(event);
        queryRepository.insertAccount(account);
        account.getBalances().forEach(queryRepository::insertBalance);
    }

    @Override
    @Transactional(value = "readDbTransactionManager")
    public void on(TransactionEvent event) {
        updateBalance(event);
        storeTransaction(event);
    }

    private void updateBalance(TransactionEvent event) {
        var currentBalance = queryRepository.selectBalance(event.getAggregateId(), event.getCurrency())
                .orElseThrow(() -> new TuumException(
                        "balance not found",
                        new NotFoundException(
                                "balance",
                                String.format("%s balance for account %s was not found", event.getCurrency(), event.getAggregateId())
                        )
                ));

        queryRepository.updateBalance(
                new BalanceDto(
                        event.getAggregateId(),
                        event.getCurrency(),
                        calculateNewBalance(currentBalance, event)
                ));
    }

    private void storeTransaction(TransactionEvent event) {
        queryRepository.insertTransaction(mapEventToDto(event));
    }

    private BigDecimal calculateNewBalance(BalanceDto currentBalance, TransactionEvent event) {
        return event.getType() == IN ?
                currentBalance.amount().add(event.getAmount()) :
                currentBalance.amount().subtract(event.getAmount());
    }
}
