package com.example.yetAnotherTodoApp.models;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TodoModelTest {
    @Test
    public void shouldCreateAndAccessData() {
        Todo todo = new Todo();
        todo.setId(10);
        todo.setTitle("TODO");
        todo.setCompleted(false);
        todo.setCreationDate(Date.from(Instant.EPOCH));
        todo.setModificationDate(Date.from(Instant.EPOCH));

        assertEquals(10, todo.getId());
        assertEquals("TODO", todo.getTitle());
        assertEquals(false, todo.getCompleted());
        assertEquals(Date.from(Instant.EPOCH), todo.getCreationDate());
        assertEquals(Date.from(Instant.EPOCH), todo.getModificationDate());
    }
}
