package com.example.yetAnotherTodoApp.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String title;
    Boolean completed;
    @Column(name = "modification_date")
    LocalDateTime modificationDate;
    @Column(name = "creation_date")
    LocalDateTime creationDate;

    public Todo() {

    }

    public Todo(long id, String title, Boolean completed, LocalDateTime modificationDate, LocalDateTime creationDate) {
        this.id = id;
        this.title = title;
        this.completed = completed;
        this.modificationDate = modificationDate;
        this.creationDate = creationDate;
    }

    public Todo(String title, Boolean completed) {
        this.title = title;
        this.completed = completed;
    }
}
