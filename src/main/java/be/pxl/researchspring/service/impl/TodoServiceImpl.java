package be.pxl.researchspring.service.impl;

import be.pxl.researchspring.api.request.CreateTodoRequest;
import be.pxl.researchspring.api.response.TodoDTO;
import be.pxl.researchspring.domain.Todo;
import be.pxl.researchspring.repository.TodoRepository;
import be.pxl.researchspring.service.TodoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoServiceImpl  implements TodoService {
    private final TodoRepository todoRepository;

    public TodoServiceImpl(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll()
                .stream()
                .map(TodoDTO::new)
                .toList();
    }

    @Override
    public Todo getTodoById(long id) {
        return null;
    }

    @Override
    public Long createTodo(CreateTodoRequest todo) {
        Todo newTodo = new Todo(todo.getTitle());
        return todoRepository.save(newTodo).getId();
    }
}
