package dev.sosnovsky.task.management.system.repository;

import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import dev.sosnovsky.task.management.system.model.Task;
import dev.sosnovsky.task.management.system.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    private Task task;

    @BeforeEach
    void setUp() {
        User user = userRepository.findAll().getFirst();
        CreateTaskDto createTaskDto = new CreateTaskDto("Test task 1", "Description task 1",
                Priority.HIGH, 2);
        Task testTask = new ModelMapper().map(createTaskDto, Task.class);
        testTask.setStatus(Status.WAITING);
        testTask.setAuthor(user);
        task = taskRepository.save(testTask);
    }

    @Test
    @DirtiesContext
    @DisplayName("Получение задачи по id")
    void findByIdTest() {
        int fakeId = 959;
        Optional<Task> taskOptional1 = taskRepository.findById(fakeId);
        assertTrue(taskOptional1.isEmpty());

        Optional<Task> taskOptional2 = taskRepository.findById(task.getId());
        assertTrue(taskOptional2.isPresent());

        Task testTask = taskOptional2.get();
        assertNotNull(testTask);
        assertEquals(task.getId(), testTask.getId());
        assertEquals(task.getStatus(), testTask.getStatus());
    }
}