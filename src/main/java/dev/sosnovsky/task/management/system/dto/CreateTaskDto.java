package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateTaskDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private Priority priority;

    private Integer executorId;
}