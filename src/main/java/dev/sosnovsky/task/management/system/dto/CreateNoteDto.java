package dev.sosnovsky.task.management.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNoteDto {
    @NotNull
    private int taskId;

    @NotBlank
    private String description;
}