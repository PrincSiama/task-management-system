package dev.sosnovsky.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dto для создания комментария")
public class CreateNoteDto {
    @Min(value = 1, message = "id должен быть числом больше нуля")
    private int taskId;

    @NotBlank
    private String description;
}