package com.example.yetAnotherTodoApp.controllers;

import com.example.yetAnotherTodoApp.models.Todo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class TodoSseController {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @GetMapping("/todos/sse")
    public SseEmitter subscribeToTodoUpdated() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    public void notifyAboutTodoCreation(Todo todo) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("todo-created")
                        .data(objectMapper.writeValueAsString(todo), MediaType.APPLICATION_JSON)
                );
            } catch (IOException e) {
                System.out.println(e);
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    public void notifyAboutTodoUpdate(Todo todo) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("todo-updated")
                        .data(objectMapper.writeValueAsString(todo), MediaType.APPLICATION_JSON)
                );
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    public void notifyAboutTodoDeletion(long id) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("todo-deleted")
                        .data(objectMapper.writeValueAsString(Map.of("id", id)), MediaType.APPLICATION_JSON)
                );
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
