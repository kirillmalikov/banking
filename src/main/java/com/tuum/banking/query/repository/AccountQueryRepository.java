package com.tuum.banking.query.repository;

import com.tuum.banking.domain.Currency;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import com.tuum.banking.query.repository.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountQueryRepository implements QueryRepository {

    @Qualifier("accountQueryMapper")
    private final AccountQueryMapper queryMapper;

    @Override
    public void insertAccount(AccountDto account) {
        queryMapper.insertAccount(account);
    }

    @Override
    public Optional<AccountDto> selectAccount(UUID accountId) {
        return queryMapper.selectAccount(accountId);
    }

    @Override
    public void insertBalance(BalanceDto balance) {
        queryMapper.insertBalance(balance);
    }

    @Override
    public void updateBalance(BalanceDto balance) {
        queryMapper.updateBalance(balance);
    }

    public Optional<BalanceDto> selectBalance(UUID accountId, Currency currency) {
        return queryMapper.selectBalance(accountId, currency.name());
    }

    @Override
    public void insertTransaction(TransactionDto transaction) {
        queryMapper.insertTransaction(transaction);
    }

    @Override
    public List<TransactionDto> selectTransactions(UUID accountId) {
        return queryMapper.selectTransactions(accountId);
    }

    @Override
    public boolean accountExists(UUID accountId) {
        return queryMapper.accountExists(accountId) > 0;
    }
}
