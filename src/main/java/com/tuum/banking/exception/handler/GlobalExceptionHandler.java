package com.tuum.banking.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tuum.banking.exception.InsufficientFundsException;
import com.tuum.banking.exception.InvalidOperationException;
import com.tuum.banking.exception.NotFoundException;
import com.tuum.banking.exception.handler.response.InsufficientFundsErrorResponse;
import com.tuum.banking.exception.handler.response.InvalidOperationErrorResponse;
import com.tuum.banking.exception.handler.response.NotFoundErrorResponse;
import com.tuum.banking.exception.handler.response.ValidationErrorResponse;
import com.tuum.banking.exception.handler.response.ValidationErrorRow;
import com.tuum.banking.exception.handler.response.ValidationErrorType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.tuum.banking.exception.handler.response.ValidationErrorType.ARGUMENT_NOT_VALID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseBody
    public NotFoundErrorResponse handleValidationException(NotFoundException e, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.NOT_FOUND.value());

        return new NotFoundErrorResponse(e);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseBody
    public InsufficientFundsErrorResponse handleValidationException(
            HttpServletResponse response, InsufficientFundsException e
    ) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.CONFLICT.value());

        return new InsufficientFundsErrorResponse(e);
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseBody
    public InvalidOperationErrorResponse handleValidationException(
            HttpServletResponse response, InvalidOperationException e
    ) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.CONFLICT.value());

        return new InvalidOperationErrorResponse(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ValidationErrorResponse handleException(MethodArgumentNotValidException e, HttpServletResponse response) {
        return translateBindingResult(e.getBindingResult(), response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ValidationErrorResponse handleJsonErrors(HttpMessageNotReadableException e, HttpServletResponse response){
        var errorResponse = new ValidationErrorResponse(ARGUMENT_NOT_VALID);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        String field = null;
        String reason = e.getCause().getClass().getSimpleName();
        String message =  e.getMessage();

        if (e.getCause() instanceof InvalidFormatException error) {
            field = error.getTargetType().getSimpleName();
            reason = error.getValue().toString();
            message = "invalid type";
        }
        errorResponse.getRows().add(new ValidationErrorRow(field, reason, message));

        return errorResponse;
    }

    private ValidationErrorResponse translateBindingResult(
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        var errorResponse = new ValidationErrorResponse(ValidationErrorType.ARGUMENT_NOT_VALID);
        errorResponse.getRows().addAll(bindingResult.getFieldErrors().stream().map(error -> new ValidationErrorRow(
                error.getField(),
                error.getCodes() != null ? error.getCodes()[0].split("\\.")[0] : null,
                parseMessage(error.getDefaultMessage())
        )).toList());

        errorResponse.getRows().addAll(bindingResult.getGlobalErrors().stream().map(error -> new ValidationErrorRow(
                error.getObjectName(),
                error.getCodes() != null ? error.getCodes()[0].split("\\.")[0] : null,
                parseMessage(error.getDefaultMessage())
        )).toList());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        return errorResponse;
    }

    private static String parseMessage(String message) {
        if (message != null
                && (message.contains("java.util.Date") || message.contains("java.time.LocalDate"))
                && message.contains("java.lang.IllegalArgumentException")) {
            return message.substring(message.indexOf("java.lang.IllegalArgumentException") + 36);
        }

        return message;
    }
}
