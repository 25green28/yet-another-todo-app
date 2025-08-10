package com.example.yetAnotherTodoApp.controllers;

import com.example.yetAnotherTodoApp.models.Todo;
import com.example.yetAnotherTodoApp.models.TodoUpdateRequest;
import com.example.yetAnotherTodoApp.services.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {
    TodoService todoService;
    TodoSseController todoSseController;

    public TodoController(TodoService todoService, TodoSseController todoSseController) {
        this.todoSseController = todoSseController;
        this.todoService = todoService;
    }

    @GetMapping()
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> todos = todoService.getTodos();
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Todo> getTodo(@PathVariable long id) {
        Todo todo = todoService.getTodo(id);
        return ResponseEntity.ok(todo);
    }

    @PostMapping()
    public ResponseEntity<Todo> createTodo(@RequestBody Todo todoToCreate) {
        Todo createdTodo = todoService.createTodo(todoToCreate);
        todoSseController.notifyAboutTodoCreation(createdTodo);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{itemId}")
                .buildAndExpand(createdTodo.getId())
                .toUri();
        return ResponseEntity.created(uri).body(createdTodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable long id) {
        todoService.deleteTodo(id);
        todoSseController.notifyAboutTodoDeletion(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable long id, @RequestBody TodoUpdateRequest todoUpdateRequest) {
        Todo updatedTodo = todoService.updateTodo(id, todoUpdateRequest);
        todoSseController.notifyAboutTodoUpdate(updatedTodo);
        return ResponseEntity.ok(updatedTodo);
    }
}
