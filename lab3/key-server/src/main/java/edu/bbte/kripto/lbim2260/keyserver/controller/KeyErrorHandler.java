package edu.bbte.kripto.lbim2260.keyserver.controller;

import edu.bbte.kripto.lbim2260.keyserver.dao.exception.IdNotFoundException;
import edu.bbte.kripto.lbim2260.keyserver.dto.ErrorDto;
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
public class KeyErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDto handleNotFound(IdNotFoundException e) {
        log.info("ID not found.");
        return new ErrorDto("Not found.");
    }

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
}
