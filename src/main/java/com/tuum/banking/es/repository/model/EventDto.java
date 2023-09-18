package com.tuum.banking.es.repository.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class EventDto {

    private Long id;
    private UUID aggregateId;
    private byte[] data;
    private Long version;
    private Instant timestamp;
}
