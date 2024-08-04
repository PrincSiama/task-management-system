package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Dto для задачи")
public class TaskDto {
    private int id;

    private String title;

    private String description;

    private Status status;

    private Priority priority;

    private UserDto author;

    private UserDto executor;

    private List<NoteDto> notes;
}