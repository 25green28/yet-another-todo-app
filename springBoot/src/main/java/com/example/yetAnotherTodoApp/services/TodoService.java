package com.example.yetAnotherTodoApp.services;

import com.example.yetAnotherTodoApp.exceptions.TodoAlreadyExistsException;
import com.example.yetAnotherTodoApp.exceptions.TodoNotFoundException;
import com.example.yetAnotherTodoApp.exceptions.TodoUnexpectedSyntax;
import com.example.yetAnotherTodoApp.models.Todo;
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
        if (todo.getId() != 0) {
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

    public Todo updateTodoTitle(long id, String newTitle) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));

        todo.setTitle(newTitle);
        todo.setModificationDate(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public Todo updateTodoStatus(long id, boolean newStatus) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));

        todo.setCompleted(newStatus);
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
