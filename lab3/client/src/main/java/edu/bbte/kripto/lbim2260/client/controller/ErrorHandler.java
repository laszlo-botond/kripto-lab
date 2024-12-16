package edu.bbte.kripto.lbim2260.client.controller;

import edu.bbte.kripto.lbim2260.client.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Stream;

@ControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public final Stream<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.info("MethodArgumentNotValidException occurred", e);
        return e.getBindingResult().getFieldErrors().stream()
                .map(it -> it.getField() + " " + it.getDefaultMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleTypeMismatch(TypeMismatchException e) {
        log.info("The request contains invalid data.");
        return new ErrorDto("The request contains invalid data.");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleTypeMismatch(SecurityException e) {
        log.info("Secure communication could not be established.");
        return new ErrorDto("Secure communication could not be established.");
    }
}
