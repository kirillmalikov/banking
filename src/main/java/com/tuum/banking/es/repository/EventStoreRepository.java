package com.tuum.banking.es.repository;

import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.es.repository.model.SnapshotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventStoreRepository implements EventStore<EventDto, SnapshotDto> {

    @Qualifier("esMapper")
    private final AccountEventStoreMapper eventStoreMapper;

    @Override
    public void storeEvent(EventDto event) {
        eventStoreMapper.insertEvent(event);
    }

    @Override
    public List<EventDto> getEvents(UUID aggregateId) {
        return eventStoreMapper.selectEvents(aggregateId);
    }

    @Override
    public List<EventDto> getEvents(UUID aggregateId, long version) {
        return eventStoreMapper.selectEventsAfterVersion(aggregateId, version);
    }

    @Override
    public void storeSnapshot(SnapshotDto snapshot) {
        eventStoreMapper.insertSnapshot(snapshot);
    }

    @Override
    public Optional<SnapshotDto> getSnapshot(UUID aggregateId) {
        return eventStoreMapper.selectSnapshot(aggregateId);
    }

    @Override
    public boolean accountExists(UUID aggregateId) {
        return eventStoreMapper.exists(aggregateId) > 0;
    }
}
