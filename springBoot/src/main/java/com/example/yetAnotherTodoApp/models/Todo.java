package com.example.yetAnotherTodoApp.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Todo {
    @Id
    @GeneratedValue()
    long id;
    String title;
    Boolean completed;
    Date modificationDate;
    Date creationDate;
}
