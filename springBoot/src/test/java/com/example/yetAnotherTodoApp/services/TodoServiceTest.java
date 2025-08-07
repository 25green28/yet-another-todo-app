package com.example.yetAnotherTodoApp.services;

import com.example.yetAnotherTodoApp.exceptions.*;
import com.example.yetAnotherTodoApp.models.Todo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class TodoServiceTest {
    @Autowired
    TodoService todoService;

    @Test
    public void shouldGetAllTodos() {
        List<Todo> todoList = todoService.getTodos();
        List<Todo> expectedTodos = List.of(
                new Todo(
                        1,
                        "Buy bread",
                        false,
                        LocalDateTime.of(2025, 8, 6, 12, 0, 0),
                        LocalDateTime.of(2025, 8, 6, 12, 0, 0)
                ),
                new Todo(
                        2,
                        "Repair chair",
                        true,
                        LocalDateTime.of(2025, 8, 7, 14, 0, 0),
                        LocalDateTime.of(2025, 8, 7, 10, 0, 0)
                ),
                new Todo(
                        3,
                        "Call a friend",
                        false,
                        LocalDateTime.of(2025, 8, 8, 15, 0, 0),
                        LocalDateTime.of(2025, 8, 8, 15, 0, 0)
                ),
                new Todo(
                        4,
                        "Tidy up my room",
                        true,
                        LocalDateTime.of(2025, 8, 9, 9, 0, 0),
                        LocalDateTime.of(2025, 8, 8, 23, 0, 0)
                ),
                new Todo(
                        5,
                        "Dishes",
                        false,
                        LocalDateTime.of(2025, 8, 10, 8, 0, 0),
                        LocalDateTime.of(2025, 8, 10, 8, 0, 0)
                )
        );

        assertEquals(todoList.size(), expectedTodos.size());

        for (int i = 0; i < todoList.size(); i++) {
            assertEquals(todoList.get(i), expectedTodos.get(i));
        }
    }

    @Test
    public void shouldGetSingleTodo() {
        Todo todo = todoService.getTodo(2);
        Todo expectedTodo = new Todo(
                2,
                "Repair chair",
                true,
                LocalDateTime.of(2025, 8, 7, 14, 0, 0),
                LocalDateTime.of(2025, 8, 7, 10, 0, 0)
        );

        assertNotNull(todo);
        assertEquals(todo, expectedTodo);
    }

    @Test
    public void shouldThrowAnExceptionWhenTodoNotFound() {
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.getTodo(9999);
        });
    }

    @Test
    @Transactional
    public void shouldAddNewTodo() {
        Todo todoToCreate = new Todo();
        todoToCreate.setTitle("Write the CV");
        todoToCreate.setCompleted(false);

        Todo createdTodo = todoService.createTodo(todoToCreate);
        checkTodo(createdTodo, todoToCreate);

        createdTodo = todoService.getTodo(createdTodo.getId());
        checkTodo(createdTodo, todoToCreate);
    }

    static void checkTodo(Todo createdTodo, Todo originalTodo) {
        assertNotNull(createdTodo);
        assertEquals(createdTodo.getTitle(), originalTodo.getTitle());
        assertEquals(createdTodo.getCompleted(), originalTodo.getCompleted());
        assertNotNull(createdTodo.getCreationDate());
        assertNotNull(createdTodo.getModificationDate());
    }

    @Test
    public void shouldNotCreateDuplicatedTodo() {
        assertThrows(TodoAlreadyExistsException.class, () -> {
            todoService.createTodo(new Todo("Dishes", false));
        });
    }

    @Test
    public void shouldThrowAnExceptionWhenCreatingTodoWithIndex() {
        Todo expectedTodo = todoService.getTodo(1);

        assertThrows(TodoUnexpectedSyntax.class, () -> {
            Todo newTodo = new Todo();
            newTodo.setTitle("Write to friend");
            newTodo.setCompleted(true);
            newTodo.setId(1);

            todoService.createTodo(newTodo);
        });

        Todo shouldBeUnmodifiedTodo = todoService.getTodo(1);

        // Check if the record is really untouched
        assertEquals(expectedTodo.getTitle(), shouldBeUnmodifiedTodo.getTitle());
        assertEquals(expectedTodo.getCompleted(), shouldBeUnmodifiedTodo.getCompleted());
        assertEquals(expectedTodo.getId(), shouldBeUnmodifiedTodo.getId());
        assertEquals(expectedTodo.getCreationDate(), shouldBeUnmodifiedTodo.getCreationDate());
        assertEquals(expectedTodo.getModificationDate(), shouldBeUnmodifiedTodo.getModificationDate());
    }

    @Test
    @Transactional
    public void shouldUpdateTodoTitle() {
        long todoId = 1;
        String newTitle = "Pass a driving test";
        Todo modifiedTodo = todoService.updateTodoTitle(todoId, newTitle);
        checkUpdatedTodoTitle(modifiedTodo, todoId, newTitle);

        modifiedTodo = todoService.getTodo(todoId);
        checkUpdatedTodoTitle(modifiedTodo, todoId, newTitle);
    }

    private void checkUpdatedTodoTitle(Todo modifiedTodo, long expectedTodoId, String newTitle) {
        assertEquals(expectedTodoId, modifiedTodo.getId());
        assertEquals(newTitle, modifiedTodo.getTitle());
        assertNotEquals(modifiedTodo.getModificationDate(), modifiedTodo.getCreationDate());
    }

    @Test
    public void shouldThrownAnExceptionWhenUpdatingTodoTitleWithWrongIndex() {
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.updateTodoTitle(9999, "Pass a driving test");
        });
    }

    @Test
    @Transactional
    public void shouldUpdateTodoStatus() {
        long todoId = 1;
        boolean newStatus = true;
        Todo modifiedTodo = todoService.updateTodoStatus(todoId, newStatus);
        checkUpdatedTodoStatus(modifiedTodo, todoId, newStatus);

        modifiedTodo = todoService.getTodo(todoId);
        checkUpdatedTodoStatus(modifiedTodo, todoId, newStatus);
    }

    private void checkUpdatedTodoStatus(Todo modifiedTodo, long todoIndex, boolean newStatus) {
        assertEquals(todoIndex, modifiedTodo.getId());
        assertEquals(newStatus, modifiedTodo.getCompleted());
        assertNotEquals(modifiedTodo.getModificationDate(), modifiedTodo.getCreationDate());
    }

    @Test
    public void shouldThrowAnExceptionWhenUpdatingTodoStatusWithWrongIndex() {
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.updateTodoStatus(9999, false);
        });
    }

    @Test
    @Transactional
    public void shouldRemoveTodo() {
        assertDoesNotThrow(() -> todoService.getTodo(1));
        todoService.deleteTodo(1);
        assertThrows(TodoNotFoundException.class, () -> {
            todoService.getTodo(1);
        });
    }
}
