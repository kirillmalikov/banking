package com.tuum.banking.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.es.mq.AccountEventPublisher;
import com.tuum.banking.es.repository.EventStore;
import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.es.repository.model.SnapshotDto;
import com.tuum.banking.exception.ConcurrencyException;
import com.tuum.banking.exception.NotFoundException;
import com.tuum.banking.exception.TuumException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountEventSourcingHandler implements EventSourcingHandler<AccountAggregate> {

    public static final int SNAPSHOT_FREQUENCY = 5;

    private final EventStore<EventDto, SnapshotDto> eventStore;
    private final AccountEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void save(AccountAggregate aggregate) {
        final var events = new ArrayList<>(aggregate.getChanges());
        storeEvents(events);

        if (aggregate.getVersion() % SNAPSHOT_FREQUENCY == 0) {
            storeSnapshot(aggregate);
        }

        publishEvents(events);
    }

    @Override
    public AccountAggregate recreateAggregate(UUID aggregateId) {
        var aggregate = getSnapshotOrElseNew(aggregateId);
        applyEvents(aggregate);

        return aggregate;
    }

    @SneakyThrows(JsonProcessingException.class)
    private void storeEvents(List<BaseEvent> events) {
        for (BaseEvent event : events) {
            try {
                eventStore.storeEvent(
                        EventDto.builder()
                                .aggregateId(event.getAggregateId())
                                .data(objectMapper.writeValueAsBytes(event))
                                .version(event.getVersion())
                                .build()
                );
            } catch (SQLException e) {
                throw new TuumException("operation is not valid", new ConcurrencyException());
            }
        }
    }

    private void publishEvents(List<BaseEvent> events) {
        events.forEach(eventPublisher::publish);
    }

    private void storeSnapshot(AccountAggregate aggregate) {
        aggregate.toSnapshot();
        try {
            eventStore.storeSnapshot(SnapshotDto
                                             .builder()
                                             .aggregateId(aggregate.getId())
                                             .data(objectMapper.writeValueAsBytes(aggregate))
                                             .version(aggregate.getVersion())
                                             .build());
        } catch (Exception ignored) {}
    }

    private AccountAggregate getSnapshotOrElseNew(UUID aggregateId) {
        checkAccountExists(aggregateId);
        var snapshot = eventStore.getSnapshot(aggregateId);

        return snapshot.map(it -> {
            try {
                return objectMapper.readValue(it.getData(), AccountAggregate.class);
            } catch (IOException ignored) {
                return null;
            }
        }).orElse(new AccountAggregate(aggregateId));
    }

    private void checkAccountExists(UUID aggregateId) {
        if (!eventStore.accountExists(aggregateId)) {
            throw new NotFoundException("account", String.format("account with id = %s was not found", aggregateId));
        }
    }

    private void applyEvents(AccountAggregate aggregate) {
        List<EventDto> events = eventStore.getEvents(aggregate.getId(), aggregate.getVersion());
        events.forEach(event -> {
            try {
                aggregate.raise(objectMapper.readValue(event.getData(), BaseEvent.class));
            } catch (IOException e) {
                throw new TuumException("unexpected error occurred during event deserialization", e);
            }
        });
    }
}
