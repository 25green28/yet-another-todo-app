package com.example.yetAnotherTodoApp.controllers;

import com.example.yetAnotherTodoApp.exceptions.TodoException;
import com.example.yetAnotherTodoApp.exceptions.TodoUnexpectedSyntax;
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
import org.springframework.test.web.servlet.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = {TodoSseController.class, TodoController.class})
public class TodoSseControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TodoService todoService;

    private final LocalDateTime time = LocalDateTime.now();

    @Test
    public void shouldReturnTodoInstanceWhenCreationPerformed() throws Exception {
        Todo newTodo = new Todo(5, "Listen to relaxing music", false, time, time);
        when(todoService.createTodo(any())).thenReturn(newTodo);

        MvcResult result = mockMvc.perform(get("/todos/sse").accept(MediaType.TEXT_EVENT_STREAM)).andReturn();

        mockMvc.perform(post("/todos").content(asJsonString(newTodo)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        checkResponse(result, "todo-created", asJsonString(newTodo));

        verify(todoService).createTodo(any());
    }

    @Test
    public void shouldNotTriggerSseWhenCreationIsNotSuccessful() throws Exception {
        when(todoService.createTodo(any())).thenThrow(TodoException.class);

        MvcResult result = mockMvc.perform(get("/todos/sse").accept(MediaType.TEXT_EVENT_STREAM)).andReturn();
        mockMvc.perform(post("/todos").content(asJsonString(new Todo())).contentType(MediaType.APPLICATION_JSON));

        assertEquals(0, result.getResponse().getContentAsString().length());
        verify(todoService).createTodo(any());
    }

    @Test
    public void shouldReturnTodoInstanceWhenUpdatePerformed() throws Exception {
        Todo todo = new Todo(1, "New title", false, time, time);
        when(todoService.updateTodo(anyLong(), any())).thenReturn(todo);

        MvcResult result = mockMvc.perform(get("/todos/sse").accept(MediaType.TEXT_EVENT_STREAM)).andReturn();

        mockMvc.perform(put("/todos/{id}", todo.getId()).content(asJsonString(new TodoUpdateRequest("New title", false))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        checkResponse(result, "todo-updated", asJsonString(todo));

        verify(todoService).updateTodo(anyLong(), any());
    }

    @Test
    public void shouldNotTriggerSseWhenUpdateIsNotSuccessful() throws Exception {
        when(todoService.updateTodo(anyLong(), any())).thenThrow(TodoException.class);

        MvcResult result = mockMvc.perform(get("/todos/sse").accept(MediaType.TEXT_EVENT_STREAM)).andReturn();
        mockMvc.perform(put("/todos/{id}", 1).content(asJsonString(new TodoUpdateRequest())).contentType(MediaType.APPLICATION_JSON));

        assertEquals(0, result.getResponse().getContentAsString().length());
        verify(todoService).updateTodo(anyLong(), any());
    }

    @Test
    public void shouldReturnTodoIndexWhenDeleteUpdatePerformed() throws Exception {
        Long todoId = 1L;
        MvcResult result = mockMvc.perform(get("/todos/sse").accept(MediaType.TEXT_EVENT_STREAM)).andReturn();

        mockMvc.perform(delete("/todos/{id}", 1))
                .andExpect(status().isNoContent());

        checkResponse(result, "todo-deleted", "{\"id\":%s}".formatted(todoId));

        verify(todoService).deleteTodo(todoId);
    }

    @Test
    public void shouldNotTriggerSseWhenDeleteIsNotSuccessful() throws Exception {
        doThrow(TodoException.class).when(todoService).deleteTodo(anyLong());

        MvcResult result = mockMvc.perform(get("/todos/sse").accept(MediaType.TEXT_EVENT_STREAM)).andReturn();
        mockMvc.perform(delete("/todos/{id}", 1));

        assertEquals(0, result.getResponse().getContentAsString().length());
        verify(todoService).deleteTodo(anyLong());
    }

    private void checkResponse(MvcResult result, String expectedEvent, String expectedData) throws Exception {
        String response = result.getResponse().getContentAsString();

        String[] events = response.split("\n\n");
        assertEquals(1, events.length);

        String[] lines = events[0].split("\n");
        assertEquals(2, lines.length);

        for (String line : lines) {
            if (line.startsWith("event:")) {
                assertEquals(expectedEvent, line.substring("event:".length()).trim());
            } else if (line.startsWith("data:")) {
                assertEquals(expectedData, line.substring("data:".length()).trim());
            }
        }
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
}
