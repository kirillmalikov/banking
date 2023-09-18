package com.tuum.banking.exception.handler.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.TreeSet;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    private ValidationErrorType type;

    private final TreeSet<ValidationErrorRow> rows = new TreeSet<>(ValidationErrorRow::compareTo);
}
