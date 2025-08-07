package com.example.yetAnotherTodoApp.exceptions;

public class TodoAlreadyExistsException extends TodoException {
    public TodoAlreadyExistsException(String name) {
        super("Todo with name %s already exists in the database".formatted(name));
    }
}
