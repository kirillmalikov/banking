package com.tuum.banking.es;

import com.tuum.banking.domain.AggregateRoot;

import java.util.UUID;

public interface EventSourcingHandler<A extends AggregateRoot> {

    void save(A aggregate);

    A recreateAggregate(UUID aggregateId);
}
