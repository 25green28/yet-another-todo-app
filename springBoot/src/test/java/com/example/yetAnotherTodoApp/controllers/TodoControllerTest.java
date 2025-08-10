package com.example.yetAnotherTodoApp.controllers;

import com.example.yetAnotherTodoApp.exceptions.*;
import com.example.yetAnotherTodoApp.models.*;
import com.example.yetAnotherTodoApp.services.TodoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = {TodoSseController.class, TodoController.class, TodoControllerAdvice.class})
public class TodoControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TodoService todoService;

    static LocalDateTime time = LocalDateTime.of(2025, 8, 07, 17, 9, 00);
    static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    public void shouldReturnTodosList() throws Exception {
        List<Todo> todoList = List.of(
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

        when(todoService.getTodos()).thenReturn(todoList);

        mockMvc.perform(get("/todos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(todoList.size())))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(
                    todoList.stream().map((todo -> todo.getId().intValue())).toArray(Integer[]::new)
            )))
            .andExpect(jsonPath("$..title", containsInAnyOrder(
                    todoList.stream().map(Todo::getTitle).toArray(String[]::new)
            )))
            .andExpect(jsonPath("$..completed", containsInAnyOrder(
                    todoList.stream().map(Todo::getCompleted).toArray(Boolean[]::new)
            )))
            .andExpect(jsonPath("$..creationDate", containsInAnyOrder(
                todoList.stream().map((todo) -> todo.getCreationDate().format(dateTimeFormat)).toArray(String[]::new)
            )))
            .andExpect(jsonPath("$..modificationDate", containsInAnyOrder(
                todoList.stream().map((todo) -> todo.getModificationDate().format(dateTimeFormat)).toArray(String[]::new)
            )));

        verify(todoService).getTodos();
    }

    @Test
    public void shouldReturnSingleTodo() throws Exception {
        when(todoService.getTodo(anyLong())).thenReturn(new Todo(1, "Take a bus", false, time, time));

        mockMvc.perform(get("/todos/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Take a bus"))
            .andExpect(jsonPath("$.completed").value("false"))
            .andExpect(jsonPath("$.modificationDate").value(time.format(dateTimeFormat)))
            .andExpect(jsonPath("$.creationDate").value(time.format(dateTimeFormat)));

        verify(todoService).getTodo(1);
    }

    @Test
    public void shouldReturn404WhenSingleTodoIsNotFound() throws Exception {
        when(todoService.getTodo(anyLong())).thenThrow(TodoNotFoundException.class);

        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isNotFound());

        verify(todoService).getTodo(anyLong());
    }

    @Test
    public void shouldCreateNewTodo() throws Exception {
        Todo newTodo = new Todo(5, "Listen to relaxing music", false, time, time);

        when(todoService.createTodo(any())).thenReturn(newTodo);

        mockMvc.perform(post("/todos").content(asJsonString(newTodo)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/todos/%s".formatted(newTodo.getId())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(newTodo.getId()))
                .andExpect(jsonPath("$.title").value(newTodo.getTitle()))
                .andExpect(jsonPath("$.completed").value(newTodo.getCompleted()))
                .andExpect(jsonPath("$.modificationDate").value(newTodo.getModificationDate().format(dateTimeFormat)))
                .andExpect(jsonPath("$.creationDate").value(newTodo.getCreationDate().format(dateTimeFormat)));

        verify(todoService).createTodo(any());
    }

    @Test
    public void shouldReturn400WhenCreatingTodoWithInvalidData() throws Exception {
        when(todoService.createTodo(any())).thenThrow(TodoUnexpectedSyntax.class);

        mockMvc.perform(post("/todos").content(asJsonString(new TodoUpdateRequest("sth"))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(todoService).createTodo(any());
    }

    @Test
    public void shouldReturn409WhenCreatingTodoWithDuplicatedName() throws Exception {
        when(todoService.createTodo(any())).thenThrow(TodoAlreadyExistsException.class);

        mockMvc.perform(post("/todos").content(asJsonString(new TodoUpdateRequest("sth"))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(todoService).createTodo(any());
    }

    protected static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldDeleteTodo() throws Exception {
        mockMvc.perform(delete("/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService).deleteTodo(1);
    }

    @Test
    public void shouldThrow404WhenDeletingNotFoundTodo() throws Exception {
        doThrow(TodoNotFoundException.class).when(todoService).deleteTodo(9999L);

        mockMvc.perform(delete("/todos/{id}", 9999))
                .andExpect(status().isNotFound());

        verify(todoService).deleteTodo(9999);
    }

    @Test
    public void shouldUpdateTodo() throws Exception {
        Todo todo = new Todo(1, "New title", false, time, time);

        when(todoService.updateTodo(anyLong(), any())).thenReturn(todo);

        mockMvc.perform(put("/todos/{id}", todo.getId()).content(asJsonString(new TodoUpdateRequest("New title", false))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todo.getId()))
                .andExpect(jsonPath("$.title").value(todo.getTitle()))
                .andExpect(jsonPath("$.completed").value(todo.getCompleted()))
                .andExpect(jsonPath("$.modificationDate").value(todo.getModificationDate().format(dateTimeFormat)))
                .andExpect(jsonPath("$.creationDate").value(todo.getCreationDate().format(dateTimeFormat)));

        verify(todoService).updateTodo(anyLong(), any());
    }

    @Test
    public void shouldReturn404WhenUpdatingNotFoundTodo() throws Exception {
        when(todoService.updateTodo(anyLong(), any())).thenThrow(new TodoNotFoundException(-1));

        mockMvc.perform(put("/todos/{id}", 999).content(asJsonString(new TodoUpdateRequest("sth"))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(todoService).updateTodo(anyLong(), any());
    }

    @Test
    public void shouldReturn400WhenUpdatingTodoWithInvalidData() throws Exception {
        when(todoService.updateTodo(anyLong(), any())).thenThrow(new TodoUnexpectedSyntax("unexpected syntax"));

        mockMvc.perform(put("/todos/{id}", 1).content(asJsonString(new TodoUpdateRequest("sth"))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(todoService).updateTodo(anyLong(), any());
    }

    @Test
    public void shouldReturn409WhenUpdatingWithDuplicatedName() throws Exception {
        when(todoService.updateTodo(anyLong(), any())).thenThrow(new TodoAlreadyExistsException("name"));

        mockMvc.perform(put("/todos/{id}", 1).content(asJsonString(new TodoUpdateRequest("sth"))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        verify(todoService).updateTodo(anyLong(), any());
    }
}
