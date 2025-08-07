package com.example.yetAnotherTodoApp.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class TodoModelTest {
    @Test
    public void shouldCreateAndAccessData() {
        Todo todo = new Todo();
        todo.setId(10L);
        todo.setTitle("TODO");
        todo.setCompleted(false);
        todo.setCreationDate(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(2)));
        todo.setModificationDate(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(2)));

        assertEquals(10, todo.getId());
        assertEquals("TODO", todo.getTitle());
        assertEquals(false, todo.getCompleted());
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(2)), todo.getCreationDate());
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(2)), todo.getModificationDate());
    }
}
