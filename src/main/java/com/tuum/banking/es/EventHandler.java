package com.tuum.banking.es;

import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.TransactionEvent;

public interface EventHandler {

    void on(AccountCreatedEvent event);

    void on(TransactionEvent event);
}
