CREATE TABLE "event"
(
    id           BIGSERIAL PRIMARY KEY    NOT NULL,
    aggregate_id UUID                     NOT NULL,
    data         BYTEA                    NOT NULL,
    version      BIGINT                   NOT NULL,
    timestamp    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE (aggregate_id, version)
);

CREATE INDEX ON "event" (aggregate_id);

CREATE TABLE "snapshot"
(
    id           BIGSERIAL PRIMARY KEY    NOT NULL,
    aggregate_id UUID UNIQUE              NOT NULL,
    data         BYTEA                    NOT NULL,
    version      BIGINT                   NOT NULL,
    timestamp    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX ON "snapshot" (aggregate_id);
