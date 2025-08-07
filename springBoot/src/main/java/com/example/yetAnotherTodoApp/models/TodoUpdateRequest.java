package com.example.yetAnotherTodoApp.models;

import lombok.Data;

@Data
public class TodoUpdateRequest {
    private String title;
    private Boolean completed;

    public TodoUpdateRequest() {

    }

    public TodoUpdateRequest(String title) {
        this.title = title;
    }

    public TodoUpdateRequest(Boolean completed) {
        this.completed = completed;
    }

    public TodoUpdateRequest(String title, Boolean completed) {
        this.title = title;
        this.completed = completed;
    }
}
