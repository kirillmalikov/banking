CREATE TABLE "account"
(
    id          UUID PRIMARY KEY NOT NULL,
    customer_id UUID             NOT NULL,
    country     VARCHAR(3)       NOT NULL
);

CREATE TABLE "balance"
(
    account_id UUID REFERENCES "account" (id) NOT NULL,
    currency   VARCHAR(3)                     NOT NULL,
    amount     DECIMAL(20, 2)                 NOT NULL
);

CREATE INDEX ON "balance" (account_id);

CREATE TABLE "transaction"
(
    id          UUID PRIMARY KEY                       NOT NULL,
    account_id  UUID REFERENCES "account" (id)         NOT NULL,
    type        VARCHAR(3)                             NOT NULL,
    currency    VARCHAR(3)                             NOT NULL,
    amount      DECIMAL(20, 2)                         NOT NULL,
    description VARCHAR                                NOT NULL,
    timestamp   TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

CREATE INDEX ON "transaction" (account_id);
