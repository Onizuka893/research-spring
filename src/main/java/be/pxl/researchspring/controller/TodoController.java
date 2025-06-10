package be.pxl.researchspring.controller;

import be.pxl.researchspring.api.response.TodoDTO;
import be.pxl.researchspring.domain.Todo;
import be.pxl.researchspring.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/todos")
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<TodoDTO> getAllTodos()
    {
        return todoService.getAllTodos();
    }

    @PostMapping
    public ResponseEntity<Todo> createTodo(@RequestParam String title) {
        Todo todo = todoService.create(title);
        return ResponseEntity.status(HttpStatus.CREATED).body(todo);
    }

    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<Todo>> createTodoAsync(@RequestParam String title) {
        return todoService.createAsync(title)
                .thenApply(todo -> ResponseEntity.status(HttpStatus.CREATED).body(todo));
    }

    @PatchMapping("/{id}/completed")
    public ResponseEntity<Void> updateCompleted(
            @PathVariable Long id,
            @RequestParam Boolean completed) {
        todoService.updateCompleted(id, completed);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/async/{id}/completed")
    public CompletableFuture<ResponseEntity<Void>> updateCompletedAsync(
            @PathVariable Long id,
            @RequestParam Boolean completed) {
        return todoService.updateCompletedAsync(id, completed)
                .thenApply(v -> ResponseEntity.ok().build());
    }
}
