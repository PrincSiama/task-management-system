package dev.sosnovsky.task.management.system.repository;

import dev.sosnovsky.task.management.system.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Integer> {
}
