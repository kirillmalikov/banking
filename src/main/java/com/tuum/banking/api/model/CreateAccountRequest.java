package com.tuum.banking.api.model;

import com.tuum.banking.domain.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateAccountRequest(
        @NotNull
        UUID customerId,
        @Size(min = 2, max = 2)
        @NotBlank
        String country,
        @NotEmpty
        Set<@NotNull Currency> currencies
) {}
