package ru.practicum.yandex.shared.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RestControllerAdvice
public class EwmServiceExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(CONFLICT)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        return ErrorResponse.builder()
                .errors(getStackTraceAsString(e))
                .message(e.getLocalizedMessage())
                .reason("Integrity constraint has been violated")
                .status(CONFLICT)
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ErrorResponse.builder()
                .errors(getStackTraceAsString(e))
                .message(e.getLocalizedMessage())
                .reason("Integrity constraint has been violated")
                .status(BAD_REQUEST)
                .build();
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}