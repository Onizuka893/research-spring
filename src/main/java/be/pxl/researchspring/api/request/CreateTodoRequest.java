package be.pxl.researchspring.api.request;

import jakarta.validation.constraints.NotBlank;

public class CreateTodoRequest {
    @NotBlank(message = "title cannot be blank")
    private String title;

    public String getTitle() {return this.title;}
}
