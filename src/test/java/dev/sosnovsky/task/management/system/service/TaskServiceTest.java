package dev.sosnovsky.task.management.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import dev.sosnovsky.task.management.system.model.Task;
import dev.sosnovsky.task.management.system.model.User;
import dev.sosnovsky.task.management.system.repository.NoteRepository;
import dev.sosnovsky.task.management.system.repository.TaskRepository;
import dev.sosnovsky.task.management.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    private TaskService taskService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private UserPrincipalService userPrincipalService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, userRepository, noteRepository, userPrincipalService,
                new ModelMapper(), new ObjectMapper());
    }

    @Test
    @DirtiesContext
    @DisplayName("Создание задачи со всеми полями")
    void createTaskWithAllFieldsTest() {
        int testAuthorId = 101;
        int testExecutorId = 201;
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 1", "description task 1",
                Priority.HIGH, testExecutorId);

        Principal principal = () -> "test@user.com";

        User testAuthor = new User();
        testAuthor.setId(testAuthorId);

        User testExecutor = new User();
        testExecutor.setId(testExecutorId);

        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(testAuthor);
        when(userRepository.findById(testExecutorId)).thenReturn(Optional.of(testExecutor));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Task.class));

        TaskDto taskDto = taskService.create(createTaskDto, principal);

        assertEquals(testAuthorId, taskDto.getAuthor().getId());
        assertEquals(testExecutorId, taskDto.getExecutor().getId());
        assertEquals(createTaskDto.getTitle(), taskDto.getTitle());
        assertEquals(createTaskDto.getDescription(), taskDto.getDescription());
        assertEquals(createTaskDto.getPriority(), taskDto.getPriority());
        assertEquals(Status.WAITING, taskDto.getStatus());
    }

    @Test
    @DirtiesContext
    @DisplayName("Создание задачи без приоритета и исполнителя")
    void createTaskWithoutPriorityAndExecutorTest() {
        int testAuthorId = 102;
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 2", "description task 2");

        Principal principal = () -> "test@user.com";

        User testAuthor = new User();
        testAuthor.setId(testAuthorId);

        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(testAuthor);
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Task.class));

        TaskDto taskDto = taskService.create(createTaskDto, principal);

        assertEquals(testAuthorId, taskDto.getAuthor().getId());
        assertNull(taskDto.getExecutor());
        assertEquals(createTaskDto.getTitle(), taskDto.getTitle());
        assertEquals(createTaskDto.getDescription(), taskDto.getDescription());
        assertEquals(Priority.MIDDLE, taskDto.getPriority());
        assertEquals(Status.WAITING, taskDto.getStatus());
    }

}
