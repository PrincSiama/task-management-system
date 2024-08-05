package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dto для создания задачи")
public class CreateTaskDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private Priority priority;

    private Integer executorId;

    public CreateTaskDto(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public CreateTaskDto(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public CreateTaskDto(String title, String description, Integer executorId) {
        this.title = title;
        this.description = description;
        this.executorId = executorId;
    }
}