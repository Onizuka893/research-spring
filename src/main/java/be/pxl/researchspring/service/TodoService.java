package be.pxl.researchspring.service;

import be.pxl.researchspring.api.response.TodoDTO;
import be.pxl.researchspring.domain.Todo;
import java.util.concurrent.CompletableFuture;


import java.util.List;

public interface TodoService {
    List<TodoDTO> getAllTodos();

    Todo create(String title);
    void updateCompleted(Long id, Boolean completed);
    void delete(Long id);

    // Asynchronous operations for concurrent processing
    CompletableFuture<Todo> createAsync(String title);
    CompletableFuture<Void> updateCompletedAsync(Long id, Boolean completed);
    CompletableFuture<Void> deleteAsync(Long id);
}
