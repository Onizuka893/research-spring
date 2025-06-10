package be.pxl.researchspring.service;

import be.pxl.researchspring.api.request.CreateTodoRequest;
import be.pxl.researchspring.api.response.TodoDTO;
import be.pxl.researchspring.domain.Todo;

import java.util.List;

public interface TodoService {
    List<TodoDTO> getAllTodos();

    Todo getTodoById(long id);

    Long createTodo(CreateTodoRequest todo);
}
