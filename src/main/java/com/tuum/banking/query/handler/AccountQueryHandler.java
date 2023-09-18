package com.tuum.banking.query.handler;

import com.tuum.banking.exception.NotFoundException;
import com.tuum.banking.query.GetAccountQuery;
import com.tuum.banking.query.GetTransactionQuery;
import com.tuum.banking.query.repository.QueryRepository;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountQueryHandler implements QueryHandler<AccountDto, TransactionDto> {

    private final QueryRepository queryRepository;

    @Override
    public AccountDto handle(GetAccountQuery query) {
        return queryRepository
                .selectAccount(query.accountId())
                .orElseThrow(() -> new NotFoundException(
                        "account",
                        String.format("account with id = %s was not found", query.accountId())
                ));
    }

    @Override
    public List<TransactionDto> handle(GetTransactionQuery query) {
        var accountId = query.accountId();
        if (!queryRepository.accountExists(accountId)) {
            throw new NotFoundException("account", String.format("account with id = %s was not found", accountId));
        }
        return queryRepository.selectTransactions(accountId);
    }
}
