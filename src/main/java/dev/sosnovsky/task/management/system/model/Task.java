package dev.sosnovsky.task.management.system.model;

import dev.sosnovsky.task.management.system.dto.NoteDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    /*@ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notes", joinColumns = @JoinColumn(name = "task_id"))
    @Enumerated(EnumType.STRING)
    private List<Note> notes = new ArrayList<>();*/

    /*public void addNotes(Note note) {
        notes.add(note);
    }*/
}