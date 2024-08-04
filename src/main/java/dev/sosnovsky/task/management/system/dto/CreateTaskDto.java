package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Schema(description = "Dto для создания задачи")
public class CreateTaskDto {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private Priority priority;

    private Integer executorId;
}