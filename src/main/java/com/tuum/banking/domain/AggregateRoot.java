package com.tuum.banking.domain;

import com.tuum.banking.es.events.BaseEvent;
import com.tuum.banking.exception.TuumException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public abstract class AggregateRoot<T extends BaseEvent> {

    private final UUID id;
    private long version;
    private final List<T> changes = new ArrayList<>();

    public AggregateRoot(final UUID id) {
        this.id = id;
    }

    public void apply(final T event) {
        this.validateEvent(event);

        on(event);
        changes.add(event);

        this.version++;
        event.setVersion(this.version);
    }

    public void raise(final T event) {
        this.validateEvent(event);

        on(event);

        this.version++;
        event.setVersion(this.version);
    }

    protected abstract void on(final T event);

    private void validateEvent(BaseEvent event) {
        if (event == null || !this.getId().equals(event.getAggregateId())) {
            throw new TuumException("invalid event");
        }
    }

    public void toSnapshot() {
        this.changes.clear();
    }
}
