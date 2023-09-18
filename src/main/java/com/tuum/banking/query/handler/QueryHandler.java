package com.tuum.banking.query.handler;

import com.tuum.banking.query.GetAccountQuery;
import com.tuum.banking.query.GetTransactionQuery;

import java.util.List;

public interface QueryHandler<A, T> {

    A handle(GetAccountQuery query);

    List<T> handle(GetTransactionQuery query);
}
