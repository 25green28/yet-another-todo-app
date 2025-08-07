package com.example.yetAnotherTodoApp.exceptions;

public class TodoNotFoundException extends TodoException {
    public TodoNotFoundException(long id) {
        super("Todo with id %s not found".formatted(id));
    }
}
