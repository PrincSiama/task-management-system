package dev.sosnovsky.task.management.system.dto;

import dev.sosnovsky.task.management.system.model.Note;
import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import dev.sosnovsky.task.management.system.model.User;

import java.util.List;

public class TaskDto {
    private int id;

    private String title;

    private String description;

    private Status status;

    private Priority priority;

    private User author;

    private User executor;

    private List<Note> notes;
}
