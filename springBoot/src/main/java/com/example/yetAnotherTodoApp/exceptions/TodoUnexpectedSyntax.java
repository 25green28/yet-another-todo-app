package com.example.yetAnotherTodoApp.exceptions;

public class TodoUnexpectedSyntax extends TodoException {
    public TodoUnexpectedSyntax(String message) {
        super("Unexpected syntax: %s".formatted(message));
    }
}
