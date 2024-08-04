package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.User;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NoteDto {
    private int id;

    private String description;

    private UserDto user;

    private LocalDate writeDate;
}