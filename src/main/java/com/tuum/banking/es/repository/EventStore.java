package com.tuum.banking.es.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventStore<E, S> {

    void storeEvent(E event) throws SQLException;

    List<E> getEvents(UUID aggregateId);

    List<E> getEvents(UUID aggregateId, long version);

    void storeSnapshot(S snapshot);

    Optional<S> getSnapshot(UUID aggregateId);

    boolean accountExists(UUID aggregateId);
}
