package be.pxl.researchspring.controller;

import be.pxl.researchspring.api.request.CreateTodoRequest;
import be.pxl.researchspring.api.response.TodoDTO;
import be.pxl.researchspring.domain.Todo;
import be.pxl.researchspring.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
    public ResponseEntity<Void> createTodo(@RequestBody @Valid CreateTodoRequest createTodoRequest)
    {
        todoService.createTodo(createTodoRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
