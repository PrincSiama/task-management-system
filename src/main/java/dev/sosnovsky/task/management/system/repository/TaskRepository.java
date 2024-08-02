package dev.sosnovsky.task.management.system.repository;

import dev.sosnovsky.task.management.system.model.Task;
import dev.sosnovsky.task.management.system.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAuthor(User user, Pageable pageable);
    List<Task> findByExecutor(User user, Pageable pageable);
}