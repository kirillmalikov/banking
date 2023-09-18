package com.tuum.banking.es.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({@JsonSubTypes.Type(value = AccountCreatedEvent.class, name = "AccountCreatedEvent"),

        @JsonSubTypes.Type(value = TransactionEvent.class, name = "TransactionEvent")})
@Data
@NoArgsConstructor(force = true)
public class BaseEvent implements Serializable {

    private final UUID aggregateId;
    private long version;

    public BaseEvent(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }
}
