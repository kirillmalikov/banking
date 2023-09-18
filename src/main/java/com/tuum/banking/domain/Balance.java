package com.tuum.banking.domain;

import java.math.BigDecimal;

public record Balance(Currency currency, BigDecimal amount) {}
