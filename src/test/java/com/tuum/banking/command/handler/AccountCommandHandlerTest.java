package com.tuum.banking.command.handler;

import com.tuum.banking.command.CreateAccountCommand;
import com.tuum.banking.command.TransactionCommand;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.EventSourcingHandler;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static com.tuum.banking.domain.Currency.EUR;
import static com.tuum.banking.es.events.TransactionEvent.TransactionType.IN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCommandHandlerTest {

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID customerId = UUID.randomUUID();
    private static final String country = "EE";
    private static final Currency currency = EUR;

    @Mock
    private EventSourcingHandler<AccountAggregate> eventSourcingHandler;

    @InjectMocks
    private AccountCommandHandler accountCommandHandler;

    @Test
    void createAccountHandleTest() {
        var command = new CreateAccountCommand(customerId, country, Set.of(currency));

        var aggregate = accountCommandHandler.handle(command);

        assertThat(aggregate.getId()).isNotNull();
        assertThat(aggregate.getVersion()).isEqualTo(1L);
        assertThat(aggregate.getCustomerId()).isEqualTo(customerId);
        assertThat(aggregate.getCountry()).isEqualTo(country);
        assertThat(aggregate.getChanges().size()).isEqualTo(1);
        assertThat(aggregate.getChanges().get(0)).isInstanceOf(AccountCreatedEvent.class);
        assertThat(aggregate.getBalances().size()).isEqualTo(1);
        assertThat(aggregate.getBalances().containsKey(currency)).isTrue();
        assertThat(aggregate.getBalances().get(currency)).isEqualTo(BigDecimal.ZERO);

        verify(eventSourcingHandler).save(aggregate);
    }

    @Test
    void transactionHandleTest() {
        var command = new TransactionCommand(accountId, currency, BigDecimal.ONE, IN, "test description");
        var givenAggregate = new AccountAggregate(accountId, customerId, country);
        givenAggregate.getBalances().put(currency, BigDecimal.ZERO);
        when(eventSourcingHandler.recreateAggregate(accountId)).thenReturn(givenAggregate);

        var aggregate = accountCommandHandler.handle(command);

        assertThat(aggregate.getId()).isNotNull();
        assertThat(aggregate.getVersion()).isEqualTo(1L);
        assertThat(aggregate.getCustomerId()).isEqualTo(customerId);
        assertThat(aggregate.getCountry()).isEqualTo(country);
        assertThat(aggregate.getChanges().size()).isEqualTo(1);
        assertThat(aggregate.getChanges().get(0)).isInstanceOf(TransactionEvent.class);
        assertThat(aggregate.getLastTransaction()).isNotNull();
        assertThat(aggregate.getBalances().size()).isEqualTo(1);
        assertThat(aggregate.getBalances().containsKey(EUR)).isTrue();
        assertThat(aggregate.getBalances().get(EUR)).isEqualTo(BigDecimal.ONE);

        verify(eventSourcingHandler).save(aggregate);
    }
}