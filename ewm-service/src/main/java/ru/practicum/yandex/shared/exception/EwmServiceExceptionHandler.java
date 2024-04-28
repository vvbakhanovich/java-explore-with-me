package ru.practicum.yandex.shared.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class EwmServiceExceptionHandler {

    @ExceptionHandler({
            ConstraintViolationException.class,
            EventNotModifiableException.class,
            RequestAlreadyExistsException.class,
            NotAuthorizedException.class
    })
    @ResponseStatus(CONFLICT)
    public ErrorResponse handleConstraintViolationException(Exception e) {
        return ErrorResponse.builder()
                .errors(getStackTraceAsString(e))
                .message(e.getLocalizedMessage())
                .reason("Integrity constraint has been violated.")
                .status(CONFLICT)
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        return ErrorResponse.builder()
                .errors(getStackTraceAsString(e))
                .message(e.getLocalizedMessage())
                .reason("The required object was not found.")
                .status(NOT_FOUND)
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ErrorResponse.builder()
                .errors(getStackTraceAsString(e))
                .message("Field: " + Objects.requireNonNull(e.getFieldError()).getField() +
                        ". Error = " + Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage() +
                        " Value: " + e.getFieldError().getRejectedValue())
                .reason("Incorrectly made request.")
                .status(BAD_REQUEST)
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllException(Exception e) {
        return ErrorResponse.builder()
                .errors(getStackTraceAsString(e))
                .message(e.getLocalizedMessage())
                .reason("Unexpected error occured.")
                .status(INTERNAL_SERVER_ERROR)
                .build();
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}