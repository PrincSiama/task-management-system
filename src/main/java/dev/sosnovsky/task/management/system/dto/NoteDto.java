package dev.sosnovsky.task.management.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Dto для комментария")
public class NoteDto {
    private int id;

    private String description;

    private UserDto author;

    private LocalDate writeDate;
}