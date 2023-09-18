package com.tuum.banking.domain;

import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.exception.TuumException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static com.tuum.banking.domain.Currency.EUR;
import static com.tuum.banking.es.events.TransactionEvent.TransactionType.IN;
import static com.tuum.banking.es.events.TransactionEvent.TransactionType.OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountAggregateTest {

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID customerId = UUID.randomUUID();
    private static final String country = "EE";
    private static final Currency currency = EUR;
    private AccountAggregate aggregate = makeAggregate();

    @Test
    void invalidEventTest() {
        assertThatExceptionOfType(TuumException.class).isThrownBy(() -> aggregate.apply(null));
        assertThatExceptionOfType(TuumException.class).isThrownBy(() -> aggregate.apply(new BaseEvent()));
    }

    @Test
    void applyCreatedEventTest() {
        var event = new AccountCreatedEvent(accountId, customerId, country, Set.of(currency));

        aggregate.apply(event);

        assertThat(aggregate.getVersion()).isEqualTo(1);
        assertThat(aggregate.getChanges().size()).isEqualTo(1);
        assertThat(aggregate.getChanges().get(0)).isEqualTo(event);
        assertThat(event.getVersion()).isEqualTo(aggregate.getVersion());
    }

    @Test
    void applyDepositEventTest() {
        var event = new TransactionEvent(
                accountId,
                UUID.randomUUID(),
                currency,
                BigDecimal.ONE,
                IN,
                "test description"
        );

        aggregate.apply(event);

        assertThat(aggregate.getVersion()).isEqualTo(1);
        assertThat(aggregate.getChanges().size()).isEqualTo(1);
        assertThat(aggregate.getChanges().get(0)).isEqualTo(event);
        assertThat(aggregate.getBalances().get(currency)).isEqualTo(BigDecimal.ONE);
        assertThat(aggregate.getLastTransaction()).isEqualTo(event);
        assertThat(event.getVersion()).isEqualTo(aggregate.getVersion());
    }

    @Test
    void applyWithdrawEventTest() {
        var event = new TransactionEvent(
                accountId,
                UUID.randomUUID(),
                currency,
                BigDecimal.ONE,
                OUT,
                "test description"
        );
        aggregate.getBalances().put(currency, BigDecimal.TEN);

        aggregate.apply(event);

        assertThat(aggregate.getVersion()).isEqualTo(1);
        assertThat(aggregate.getChanges().size()).isEqualTo(1);
        assertThat(aggregate.getChanges().get(0)).isEqualTo(event);
        assertThat(aggregate.getBalances().get(currency)).isEqualTo(BigDecimal.valueOf(9));
        assertThat(aggregate.getLastTransaction()).isEqualTo(event);
        assertThat(event.getVersion()).isEqualTo(aggregate.getVersion());
    }

    @Test
    void applyWithdrawEventWithInsufficientFundsTest() {
        var event = new TransactionEvent(
                accountId,
                UUID.randomUUID(),
                currency,
                BigDecimal.ONE,
                OUT,
                "test description"
        );

        assertThatExceptionOfType(TuumException.class).isThrownBy(() -> aggregate.apply(event));
    }

    @Test
    void raiseTest() {
        var event = new AccountCreatedEvent(accountId, customerId, country, Set.of(currency));

        aggregate.raise(event);

        assertThat(aggregate.getVersion()).isEqualTo(1);
        assertThat(aggregate.getChanges()).isEmpty();
        assertThat(event.getVersion()).isEqualTo(aggregate.getVersion());
    }

    @Test
    void toSnapshotTest() {
        aggregate.getChanges().add(new BaseEvent());
        assertThat(aggregate.getChanges()).isNotEmpty();

        aggregate.toSnapshot();

        assertThat(aggregate.getChanges()).isEmpty();
    }

    private AccountAggregate makeAggregate() {
        var aggregate = new AccountAggregate(accountId, customerId, country);
        aggregate.getBalances().put(currency, BigDecimal.ZERO);

        return aggregate;
    }
}