package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.User;

import java.time.LocalDate;

public class NoteDto {
    private int id;

    private String description;

    private User user;

    private LocalDate writeDate;
}