package be.pxl.researchspring.service.impl;

import be.pxl.researchspring.api.response.TodoDTO;
import be.pxl.researchspring.domain.Todo;
import be.pxl.researchspring.repository.TodoRepository;
import be.pxl.researchspring.service.TodoService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

import java.util.List;

@Service
@Transactional
public class TodoServiceImpl  implements TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoServiceImpl.class);

    @Autowired
    private TodoRepository todoRepository;

    @Override
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll()
                .stream()
                .map(TodoDTO::new)
                .toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Todo create(String title) {
        logger.info("Creating new todo with title: {}", title);
        Todo todo = new Todo(title);
        return todoRepository.save(todo);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50)
    )
    public void updateCompleted(Long id, Boolean completed) {
        logger.info("Updating todo {} completed status to: {}", id, completed);

        // Try optimistic update first
        int updated = todoRepository.updateCompleted(id, completed);

        if (updated == 0) {
            // If optimistic update failed, use pessimistic locking
            Todo todo = todoRepository.findByIdWithLock(id)
                    .orElseThrow(() -> new EntityNotFoundException("Todo not found with id: " + id));
            todo.setCompleted(completed);
            todoRepository.save(todo);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        logger.info("Deleting todo with id: {}", id);
        if (!todoRepository.existsById(id)) {
            throw new EntityNotFoundException("Todo not found with id: " + id);
        }
        todoRepository.deleteById(id);
    }

    // Asynchronous methods for concurrent operations
    @Override
    @Async
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompletableFuture<Todo> createAsync(String title) {
        return CompletableFuture.supplyAsync(() -> create(title));
    }

    @Override
    @Async
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CompletableFuture<Void> updateCompletedAsync(Long id, Boolean completed) {
        return CompletableFuture.runAsync(() -> updateCompleted(id, completed));
    }

    @Override
    @Async
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompletableFuture<Void> deleteAsync(Long id) {
        return CompletableFuture.runAsync(() -> delete(id));
    }
}
