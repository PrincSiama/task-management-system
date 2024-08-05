package dev.sosnovsky.task.management.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.ReplaceOperation;
import dev.sosnovsky.task.management.system.dto.CreateNoteDto;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.exception.NotFoundException;
import dev.sosnovsky.task.management.system.exception.NotUpdatedException;
import dev.sosnovsky.task.management.system.exception.PermissionDeniedException;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.annotation.DirtiesContext;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

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
    private final ModelMapper modelMapper = new ModelMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository, userRepository, noteRepository, userPrincipalService,
                modelMapper, objectMapper);
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

    @Test
    @DirtiesContext
    @DisplayName("Создание задачи с несуществующим исполнителем")
    void createTaskWithoutRealExecutorTest() {
        int testAuthorId = 103;
        int fakeExecutorId = 203;
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 3", "description task 3",
                fakeExecutorId);
        Principal principal = () -> "test@user.com";

        User testAuthor = new User();
        testAuthor.setId(testAuthorId);

        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(testAuthor);
        when(userRepository.findById(fakeExecutorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.create(createTaskDto, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Корректное обновление задачи автором")
    void correctUpdateTaskByAuthorTest() {
        objectMapper.findAndRegisterModules();
        int testAuthorId = 104;
        int testExecutorId = 204;
        int taskId = 304;
        String updateTitle = "update task 4";
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 4", "description task 4",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(author);
        when(taskRepository.save(Mockito.any(Task.class))).then(returnsFirstArg());

        TaskDto updateTask;
        try {
            JsonPatch jsonPatch = new JsonPatch(List.of(new ReplaceOperation(new JsonPointer("/title"),
                    new TextNode(updateTitle))));

            updateTask = taskService.update(taskId, jsonPatch, principal);
        } catch (JsonPointerException e) {
            throw new NotUpdatedException("Невозможно обновить данные пользователя", e);
        }

        assertNotNull(updateTask);
        assertEquals(updateTitle, updateTask.getTitle());
        assertEquals(task.getPriority(), updateTask.getPriority());
    }

    @Test
    @DirtiesContext
    @DisplayName("Обновление несуществующей задачи")
    void updateNonExistTaskTest() {
        int taskId = 305;
        Principal principal = () -> "test@user.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        try {
            JsonPatch jsonPatch = new JsonPatch(List.of(new ReplaceOperation(new JsonPointer("/title"),
                    new TextNode(""))));

            assertThrows(NotFoundException.class,
                    () -> taskService.update(taskId, jsonPatch, principal));
        } catch (JsonPointerException e) {
            throw new NotUpdatedException("Невозможно обновить данные пользователя", e);
        }
    }

    @Test
    @DirtiesContext
    @DisplayName("Корректное обновление задачи исполнителем")
    void correctUpdateTaskByExecutorTest() {
        objectMapper.findAndRegisterModules();
        int testAuthorId = 106;
        int testExecutorId = 206;
        int taskId = 306;
        String updateTitle = "update task 5";
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 5", "description task 5",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(author);
        when(taskRepository.save(Mockito.any(Task.class))).then(returnsFirstArg());

        TaskDto updateTask;
        try {
            JsonPatch jsonPatch = new JsonPatch(List.of(new ReplaceOperation(new JsonPointer("/title"),
                    new TextNode(updateTitle))));

            updateTask = taskService.update(taskId, jsonPatch, principal);
        } catch (JsonPointerException e) {
            throw new NotUpdatedException("Невозможно обновить данные пользователя", e);
        }

        assertNotNull(updateTask);
        assertEquals(updateTitle, updateTask.getTitle());
        assertEquals(task.getPriority(), updateTask.getPriority());
    }

    @Test
    @DirtiesContext
    @DisplayName("Обновление задачи пользователем")
    void updateTaskByUserTest() {
        objectMapper.findAndRegisterModules();
        int testAuthorId = 107;
        int testExecutorId = 207;
        int taskId = 307;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 6", "description task 6",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(new User());

        try {
            JsonPatch jsonPatch = new JsonPatch(List.of(new ReplaceOperation(new JsonPointer("/title"),
                    new TextNode(""))));

            assertThrows(PermissionDeniedException.class,
                    () -> taskService.update(taskId, jsonPatch, principal));
        } catch (JsonPointerException e) {
            throw new NotUpdatedException("Невозможно обновить данные пользователя", e);
        }
    }

    @Test
    @DirtiesContext
    @DisplayName("Корректное удаление задачи автором")
    void correctDeleteTaskByAuthorTest() {
        int testAuthorId = 108;
        int testExecutorId = 208;
        int taskId = 308;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 7", "description task 7",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(author);
        doNothing().when(taskRepository).deleteById(anyInt());

        taskService.delete(taskId, principal);

        verify(taskRepository).deleteById(anyInt());
    }

    @Test
    @DirtiesContext
    @DisplayName("Некорректное удаление задачи исполнителем")
    void incorrectDeleteTaskByExecutorTest() {
        int testAuthorId = 109;
        int testExecutorId = 209;
        int taskId = 309;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 8", "description task 8",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(executor);

        assertThrows(PermissionDeniedException.class,
                () -> taskService.delete(taskId, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Удаление несуществующей задачи")
    void deleteNonExistTaskTest() {
        int taskId = 310;
        Principal principal = () -> "test@user.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.delete(taskId, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Корректное получение задачи")
    void correctGetTaskTest() {
        int taskId = 311;
        Task task = new Task(taskId, "test task 9", "description task 9",
                Status.WAITING, Priority.HIGH, new User(), new User());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskDto taskDto = taskService.getTaskById(taskId);

        assertNotNull(taskDto);
        assertEquals(task.getTitle(), taskDto.getTitle());
    }

    @Test
    @DirtiesContext
    @DisplayName("Получение несуществующей задачи")
    void getNonExistTaskTest() {
        int taskId = 312;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.getTaskById(taskId));
    }

    @Test
    @DirtiesContext
    @DisplayName("Корректное изменение статуса задачи автором")
    void correctChangeTaskStatusByAuthorTest() {
        int testAuthorId = 113;
        int testExecutorId = 213;
        int taskId = 313;
        Status status = Status.ACCEPTED;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 10", "description task 10",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(author);
        when(taskRepository.save(Mockito.any(Task.class))).then(returnsFirstArg());

        TaskDto taskDto = taskService.changeTaskStatus(taskId, status, principal);

        assertNotNull(taskDto);
        assertEquals(status, taskDto.getStatus());
    }

    @Test
    @DirtiesContext
    @DisplayName("Изменение статуса задачи исполнителем")
    void correctChangeTaskStatusByExecutorTest() {
        int testAuthorId = 114;
        int testExecutorId = 214;
        int taskId = 314;
        Status status = Status.IN_PROGRESS;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 11", "description task 11",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(executor);
        when(taskRepository.save(Mockito.any(Task.class))).then(returnsFirstArg());

        TaskDto taskDto = taskService.changeTaskStatus(taskId, status, principal);

        assertNotNull(taskDto);
        assertEquals(status, taskDto.getStatus());
    }

    @Test
    @DirtiesContext
    @DisplayName("Изменение статуса несуществующей задачи")
    void changeNonExistsTaskStatusTest() {
        int taskId = 315;
        Status status = Status.IN_PROGRESS;
        Principal principal = () -> "test@user.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.changeTaskStatus(taskId, status, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Изменение статуса задачи пользователем")
    void changeTaskStatusByUserTest() {
        int testAuthorId = 116;
        int testExecutorId = 216;
        int taskId = 316;
        Status status = Status.IN_PROGRESS;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 12", "description task 12",
                Status.WAITING, Priority.HIGH, author, executor);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(new User());

        assertThrows(PermissionDeniedException.class,
                () -> taskService.changeTaskStatus(taskId, status, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Назначение исполнителя автором задачи")
    void setExecutorByAuthorTest() {
        int testAuthorId = 117;
        int testExecutorId = 217;
        int taskId = 317;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 13", "description task 13", author);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(testExecutorId)).thenReturn(Optional.of(executor));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(author);
        when(taskRepository.save(Mockito.any(Task.class))).then(returnsFirstArg());

        TaskDto taskDto = taskService.setExecutor(taskId, testExecutorId, principal);

        assertNotNull(taskDto);
        assertEquals(testExecutorId, taskDto.getExecutor().getId());
    }

    @Test
    @DirtiesContext
    @DisplayName("Назначение исполнителя несуществующей задачи")
    void setExecutorNonExistsTaskTest() {
        int testExecutorId = 218;
        int taskId = 318;
        Principal principal = () -> "test@user.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.setExecutor(taskId, testExecutorId, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Назначение несуществующего исполнителя")
    void setNonExistsExecutorTaskTest() {
        int testExecutorId = 219;
        int taskId = 319;
        Principal principal = () -> "test@user.com";

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(new Task()));
        when(userRepository.findById(testExecutorId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.setExecutor(taskId, testExecutorId, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Назначение исполнителя пользователем")
    void setExecutorByUserTest() {
        int testAuthorId = 120;
        int testExecutorId = 220;
        int taskId = 320;
        Principal principal = () -> "test@user.com";

        User author = new User(testAuthorId, "authorFirstName", "authorLastName",
                "author@email.com", "authorPassword");

        User executor = new User(testExecutorId, "executorFirstName", "executorLastName",
                "executor@email.com", "executorPassword");

        Task task = new Task(taskId, "test task 14", "description task 14", author);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(testExecutorId)).thenReturn(Optional.of(executor));
        when(userPrincipalService.getUserFromPrincipal(principal)).thenReturn(new User());

        assertThrows(PermissionDeniedException.class,
                () -> taskService.setExecutor(taskId, testExecutorId, principal));
    }

    @Test
    @DirtiesContext
    @DisplayName("Добавление комментария к несуществующей задаче")
    void addNoteNonExistsTaskTest() {
        Principal principal = () -> "test@user.com";

        when(taskRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> taskService.addNote(new CreateNoteDto(), principal));
    }
}