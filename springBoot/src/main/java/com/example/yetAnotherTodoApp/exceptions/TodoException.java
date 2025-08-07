package com.example.yetAnotherTodoApp.exceptions;

public class TodoException extends RuntimeException {
    String message;

    protected TodoException(String message) {
        super(message);
        this.message = message;
    }
}
