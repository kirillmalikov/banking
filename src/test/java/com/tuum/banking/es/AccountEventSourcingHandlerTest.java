package com.tuum.banking.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.es.mq.AccountEventPublisher;
import com.tuum.banking.es.repository.EventStore;
import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.es.repository.model.SnapshotDto;
import com.tuum.banking.exception.NotFoundException;
import com.tuum.banking.exception.TuumException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.tuum.banking.domain.Currency.EUR;
import static com.tuum.banking.es.events.TransactionEvent.TransactionType.IN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountEventSourcingHandlerTest {

    private static final UUID accountId = UUID.randomUUID();

    @Mock
    private EventStore<EventDto, SnapshotDto> eventStore;
    @Mock
    private AccountEventPublisher eventPublisher;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccountEventSourcingHandler eventSourcingHandler;

    @Test
    void withInvalidEventShouldThrowException() throws Exception {
        var aggregate = new AccountAggregate(accountId);
        aggregate.getChanges().add(new BaseEvent());
        when(objectMapper.writeValueAsBytes(any())).thenReturn(new byte[0]);
        doThrow(new SQLException()).when(eventStore).storeEvent(any());

        assertThatExceptionOfType(TuumException.class)
                .isThrownBy(() -> eventSourcingHandler.save(aggregate));
    }

    @Test
    void saveShouldStoreAndPublishEvents() throws Exception {
        var aggregate = new AccountAggregate(accountId);
        aggregate.getChanges().add(new BaseEvent());
        aggregate.setVersion(5);
        when(objectMapper.writeValueAsBytes(any())).thenReturn(new byte[0]);

        eventSourcingHandler.save(aggregate);

        verify(eventStore).storeSnapshot(any(SnapshotDto.class));
        verify(eventStore).storeEvent(any(EventDto.class));
        verify(eventPublisher).publish(any(BaseEvent.class));
    }

    @Test
    void whenAccountNotExistRecreateAggregateShouldThrowException() {
        when(eventStore.accountExists(accountId)).thenReturn(false);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> eventSourcingHandler.recreateAggregate(accountId));
    }

    @Test
    void recreateShouldRecreateAggregateState() throws Exception {
        var aggregate = new AccountAggregate(accountId);
        aggregate.getBalances().put(EUR, BigDecimal.ZERO);
        var snapshot = SnapshotDto
                .builder()
                .aggregateId(accountId)
                .data(new byte[0])
                .build();
        var eventDto = EventDto.builder().aggregateId(accountId).data(new byte[0]).build();
        var event = new TransactionEvent(
                accountId,
                UUID.randomUUID(),
                EUR,
                BigDecimal.TEN,
                IN,
                "test description"
        );

        when(eventStore.accountExists(accountId)).thenReturn(true);
        when(eventStore.getSnapshot(accountId)).thenReturn(Optional.of(snapshot));
        when(objectMapper.readValue(snapshot.getData(), AccountAggregate.class)).thenReturn(aggregate);
        when(eventStore.getEvents(accountId, 0L)).thenReturn(List.of(eventDto));
        when(objectMapper.readValue(eventDto.getData(), BaseEvent.class)).thenReturn(event);

        assertThat(aggregate.getBalances().get(EUR)).isEqualTo(BigDecimal.ZERO);

        var result = eventSourcingHandler.recreateAggregate(accountId);

        assertThat(result).isEqualTo(aggregate);
        assertThat(aggregate.getBalances().get(EUR)).isEqualTo(BigDecimal.TEN);
    }
}
