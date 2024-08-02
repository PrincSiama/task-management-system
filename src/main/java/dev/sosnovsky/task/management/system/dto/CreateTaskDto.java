package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private Priority priority;

    private User executor;
}