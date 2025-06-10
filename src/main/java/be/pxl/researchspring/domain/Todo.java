package be.pxl.researchspring.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private boolean completed;

    public Long getId() {
        return id;
    }

    public String getTitle() {return title;}
    public boolean isCompleted() {return completed;}

    public void setId(Long id) {this.id = id;}

    public Todo() {
    }

    public Todo(String title) {
        this.title = title;
        this.completed = false;
    }

    public void markCompleted() {this.completed = true;}
}
