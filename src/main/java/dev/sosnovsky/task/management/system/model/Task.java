package dev.sosnovsky.task.management.system.model;

import dev.sosnovsky.task.management.system.dto.NoteDto;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "executor_id")
    private User executor;

    @OneToMany
    @JoinColumn(name = "task_id")
    //todo NOTEDTO
    private List<Note> notes = new ArrayList<>();

    //todo NOTEDTO
    public void addNotes(Note noteDto) {
        notes.add(noteDto);
    }
}