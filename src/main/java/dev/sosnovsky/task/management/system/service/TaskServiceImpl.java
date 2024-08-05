package dev.sosnovsky.task.management.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.sosnovsky.task.management.system.dto.CreateNoteDto;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.exception.NotFoundException;
import dev.sosnovsky.task.management.system.exception.NotUpdatedException;
import dev.sosnovsky.task.management.system.exception.PermissionDeniedException;
import dev.sosnovsky.task.management.system.model.*;
import dev.sosnovsky.task.management.system.repository.NoteRepository;
import dev.sosnovsky.task.management.system.repository.TaskRepository;
import dev.sosnovsky.task.management.system.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.sosnovsky.task.management.system.specification.TaskSpecification.*;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final UserPrincipalService userPrincipalService;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public TaskDto create(CreateTaskDto createTaskDto, Principal principal) {
        User user = userPrincipalService.getUserFromPrincipal(principal);
        Task task = mapper.map(createTaskDto, Task.class);
        task.setStatus(Status.WAITING);
        if (task.getPriority() == null) {
            task.setPriority(Priority.MIDDLE);
        }

        Integer executorId = createTaskDto.getExecutorId();
        if (executorId != null) {
            User executor = userRepository.findById(executorId)
                    .orElseThrow(() -> new NotFoundException("Невозможно создать задачу." +
                            " Исполнитель с id " + executorId + " не найден"));
            task.setExecutor(executor);
        }
        task.setAuthor(user);
        return mapper.map(taskRepository.save(task), TaskDto.class);
    }

    @Override
    public TaskDto update(int taskId, JsonPatch jsonPatch, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Невозможно обновить задачу. Задача с id " + taskId +
                        " не найдена"));
        User user = userPrincipalService.getUserFromPrincipal(principal);
        if (user.getId() == task.getAuthor().getId() || user.getId() == task.getExecutor().getId()) {
            try {
                JsonNode jsonNode = objectMapper.convertValue(task, JsonNode.class);
                JsonNode patched = jsonPatch.apply(jsonNode);
                Task updateTask = objectMapper.treeToValue(patched, Task.class);
                return mapper.map(taskRepository.save(updateTask), TaskDto.class);
            } catch (JsonPatchException | JsonProcessingException e) {
                throw new NotUpdatedException("Невозможно обновить задачу", e);
            }
        } else {
            throw new PermissionDeniedException("Невозможно обновить задачу. " +
                    "Обновить задачу может только автор или исполнитель");
        }
    }

    @Override
    public void delete(int taskId, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Невозможно удалить задачу. Задача с id " + taskId +
                        " не найдена"));
        User user = userPrincipalService.getUserFromPrincipal(principal);
        if (user.getId() == task.getAuthor().getId()) {
            taskRepository.deleteById(taskId);
        } else {
            throw new PermissionDeniedException("Невозможно удалить задачу. " +
                    "Удалить задачу может только автор");
        }
    }

    @Override
    public TaskDto getTaskById(int taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Невозможно получить задачу. Задача с id " + taskId +
                        " не найдена"));
        return mapper.map(task, TaskDto.class);
    }

    @Override
    public List<TaskDto> getTasksByFilters(Integer authorId, Integer executorId,
                                           Status status, Priority priority, Pageable pageable) {

        Specification<Task> specification = searchParametersToSpecification(authorId, executorId, status, priority);

        return taskRepository.findAll(specification, pageable)
                .stream().map(task -> mapper.map(task, TaskDto.class)).collect(Collectors.toList());
    }

    @Override
    public TaskDto changeTaskStatus(int taskId, Status status, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Невозможно изменить статус. Задача с id " + taskId +
                        " не найдена"));
        User user = userPrincipalService.getUserFromPrincipal(principal);
        if (user.getId() == task.getAuthor().getId() || user.getId() == task.getExecutor().getId()) {
            task.setStatus(status);
            return mapper.map(taskRepository.save(task), TaskDto.class);
        } else {
            throw new PermissionDeniedException("Невозможно обновить статус задачи. " +
                    "Обновить статус задачи может только автор или исполнитель");
        }
    }

    @Override
    public TaskDto setExecutor(int taskId, int executorId, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Невозможно назначить исполнителя задачи." +
                        " Задача с id " + taskId + " не найдена"));
        User executor = userRepository.findById(executorId)
                .orElseThrow(() -> new NotFoundException("Невозможно назначить исполнителя задачи." +
                        " Пользователь с id " + executorId + " не найден"));
        User user = userPrincipalService.getUserFromPrincipal(principal);
        if (user.getId() == task.getAuthor().getId()) {
            task.setExecutor(executor);
            return mapper.map(taskRepository.save(task), TaskDto.class);
        } else {
            throw new PermissionDeniedException("Невозможно назначить исполнителя задачи. " +
                    "Назначить исполнителя может только автор задачи");
        }
    }

    @Override
    public TaskDto addNote(CreateNoteDto createNoteDto, Principal principal) {
        Task task = taskRepository.findById(createNoteDto.getTaskId())
                .orElseThrow(() -> new NotFoundException("Невозможно добавить комментарий." +
                        " Задача с id " + createNoteDto.getTaskId() + " не найдена"));
        User user = userPrincipalService.getUserFromPrincipal(principal);
        Note note = mapper.map(createNoteDto, Note.class);
        note.setAuthor(user);
        note.setWriteDate(LocalDate.now());
        noteRepository.save(note);
        return mapper.map(taskRepository.findById(task.getId()), TaskDto.class);
    }

    private Specification<Task> searchParametersToSpecification(Integer authorId, Integer executorId,
                                                                Status status, Priority priority) {
        User author = new User();
        User executor = new User();

        if (authorId != null) {
            author = userRepository.findById(authorId)
                    .orElseThrow(() -> new NotFoundException("Невозможно получить список задач. Пользователь с id "
                            + authorId + " не найден"));
        }

        if (executorId != null) {
            executor = userRepository.findById(executorId)
                    .orElseThrow(() -> new NotFoundException("Невозможно получить список задач. Пользователь с id "
                            + executorId + " не найден"));
        }

        List<Specification<Task>> specifications = new ArrayList<>();
        specifications.add(authorId == null ? null : findByAuthor(author));
        specifications.add(executorId == null ? null : findByExecutor(executor));
        specifications.add(status == null ? null : findByStatus(status));
        specifications.add(priority == null ? null : findByPriority(priority));

        return specifications.stream().filter(Objects::nonNull).reduce(Specification::and)
                .orElse((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
    }
}