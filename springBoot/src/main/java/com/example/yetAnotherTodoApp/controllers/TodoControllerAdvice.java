package com.example.yetAnotherTodoApp.controllers;

import com.example.yetAnotherTodoApp.exceptions.TodoAlreadyExistsException;
import com.example.yetAnotherTodoApp.exceptions.TodoException;
import com.example.yetAnotherTodoApp.exceptions.TodoNotFoundException;
import com.example.yetAnotherTodoApp.exceptions.TodoUnexpectedSyntax;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

@ControllerAdvice
public class TodoControllerAdvice {

    private final DefaultErrorAttributes defaultErrorAttributes = new DefaultErrorAttributes();

    @ExceptionHandler(TodoAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExistingTodo(TodoAlreadyExistsException exception, HttpServletRequest httpServletRequest) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        return ResponseEntity.status(httpStatus).body(returnJsonException("TodoAlreadyExistsException", exception.getMessage(), httpServletRequest, httpStatus));
    }

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundTodo(TodoNotFoundException exception, HttpServletRequest httpServletRequest) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus).body(returnJsonException("TodoNotFoundException", exception.getMessage(), httpServletRequest, httpStatus));
    }

    @ExceptionHandler(TodoUnexpectedSyntax.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedSyntax(TodoUnexpectedSyntax exception, HttpServletRequest httpServletRequest) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(returnJsonException("TodoUnexpectedSyntax", exception.getMessage(), httpServletRequest, httpStatus));
    }

    @ExceptionHandler(TodoException.class)
    public ResponseEntity<Void> handleGenericError(TodoException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }

    Map<String, Object> returnJsonException(String className, String message, HttpServletRequest webRequest, HttpStatus httpStatus) {
        Map<String, Object> errorAttributesMap = defaultErrorAttributes.getErrorAttributes(new ServletWebRequest(webRequest), ErrorAttributeOptions.defaults().including(ErrorAttributeOptions.Include.PATH));
        errorAttributesMap.put("error", className);
        errorAttributesMap.put("message", message);
        errorAttributesMap.put("status", httpStatus.value());

        return errorAttributesMap;
    }
}
