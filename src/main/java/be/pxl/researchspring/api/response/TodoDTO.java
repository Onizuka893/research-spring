package be.pxl.researchspring.api.response;

import be.pxl.researchspring.domain.Todo;

public class TodoDTO {
    private long id;
    private String title;
    private boolean completed;

    public TodoDTO() {
        super();
    }

    public TodoDTO(Todo todo) {
        this.id = todo.getId();
        this.title = todo.getTitle();
        this.completed = todo.isCompleted();
    }

    public long getId() {return id;}
    public String getTitle() {return title;}
    public boolean isCompleted() {return completed;}
}
