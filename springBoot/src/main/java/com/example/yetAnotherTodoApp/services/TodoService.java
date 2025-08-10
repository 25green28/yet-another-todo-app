package com.example.yetAnotherTodoApp.services;

import com.example.yetAnotherTodoApp.exceptions.TodoAlreadyExistsException;
import com.example.yetAnotherTodoApp.exceptions.TodoNotFoundException;
import com.example.yetAnotherTodoApp.exceptions.TodoUnexpectedSyntax;
import com.example.yetAnotherTodoApp.models.Todo;
import com.example.yetAnotherTodoApp.models.TodoUpdateRequest;
import com.example.yetAnotherTodoApp.repositories.TodoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {
    TodoRepository todoRepository;

    TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getTodos() {
        return todoRepository.findAll();
    }

    public Todo getTodo(long id) {
        Optional<Todo> foundTodo = todoRepository.findById(id);
        if (foundTodo.isEmpty())
            throw new TodoNotFoundException(id);
        return foundTodo.get();
    }

    public Todo createTodo(Todo todo) {
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        if (todo.getTitle() == null) {
            throw new TodoUnexpectedSyntax("creating a new todo without title isn't allowed.");
        }
        if (todo.getTitle().isEmpty()) {
            throw new TodoUnexpectedSyntax("creating a new todo with empty title isn't allowed.");
        }
        if (todo.getId() != null) {
            throw new TodoUnexpectedSyntax("creating a new todo with an id, that is already auto-generated. To perform an update use another appropriate method.");
        }
        if (todoRepository.findByTitle(todo.getTitle()).isPresent()) {
            throw new TodoAlreadyExistsException(todo.getTitle());
        }
        LocalDateTime currentTime = LocalDateTime.now();
        todo.setModificationDate(currentTime);
        todo.setCreationDate(currentTime);
        return todoRepository.save(todo);
    }

    public Todo updateTodo(long id, TodoUpdateRequest todoUpdateRequest) {
        if (todoUpdateRequest.getTitle() == null && todoUpdateRequest.getCompleted() == null) {
            throw new TodoUnexpectedSyntax("both title and completed fields are empty, please provide a value for at least one of them.");
        }

        if (todoUpdateRequest.getTitle() != null) {
            if (todoUpdateRequest.getTitle().trim().isEmpty()) {
                throw new TodoUnexpectedSyntax("the field named title must either be null or have a length greater than zero.");
            }
        }
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));

        if (todoUpdateRequest.getTitle() != null) {
            if (todoRepository.findByTitle(todoUpdateRequest.getTitle()).isPresent()) {
                throw new TodoAlreadyExistsException(todoUpdateRequest.getTitle());
            }
            todo.setTitle(todoUpdateRequest.getTitle());
        }
        if (todoUpdateRequest.getCompleted() != null) {
            todo.setCompleted(todoUpdateRequest.getCompleted());
        }
        todo.setModificationDate(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public void deleteTodo(long id) {
        if (!todoRepository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        todoRepository.deleteById(id);
    }
}
