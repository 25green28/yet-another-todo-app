package com.example.yetAnotherTodoApp.repositories;

import com.example.yetAnotherTodoApp.models.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
}
