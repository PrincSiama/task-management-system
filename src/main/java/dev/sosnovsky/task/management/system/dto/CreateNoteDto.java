package dev.sosnovsky.task.management.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNoteDto {
    @Min(value = 1, message = "id должен быть числом больше нуля")
    private int taskId;

    @NotBlank
    private String description;
}