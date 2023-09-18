package com.tuum.banking.query.repository;

import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import com.tuum.banking.query.repository.dto.TransactionDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueryRepository {

    void insertAccount(AccountDto account);

    void insertBalance(BalanceDto balance);

    void updateBalance(BalanceDto balance);

    Optional<AccountDto> selectAccount(UUID accountId);

    void insertTransaction(TransactionDto transaction);

    List<TransactionDto> selectTransactions(UUID accountId);

    boolean accountExists(UUID accountId);
}
