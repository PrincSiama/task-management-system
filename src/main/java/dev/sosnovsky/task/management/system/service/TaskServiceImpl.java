package dev.sosnovsky.task.management.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import dev.sosnovsky.task.management.system.dto.CreateNoteDto;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.NoteDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.exception.NotFoundException;
import dev.sosnovsky.task.management.system.exception.NotUpdatedException;
import dev.sosnovsky.task.management.system.exception.PermissionDeniedException;
import dev.sosnovsky.task.management.system.model.*;
import dev.sosnovsky.task.management.system.repository.TaskRepository;
import dev.sosnovsky.task.management.system.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
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
        if (user.getId() == task.getAuthor().getId() || user.getId() == task.getExecutor().getId()) {
            taskRepository.deleteById(taskId);
        } else {
            throw new PermissionDeniedException("Невозможно удалить задачу. " +
                    "Удалить задачу может только автор или исполнитель");
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
    public List<TaskDto> getTasksByAuthor(int authorId, Status status, Pageable pageable) {

        //todo добавить фильтр по статусу!!!

        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Невозможно получить список задач. Пользователь с id "
                        + authorId + " не найден"));
        return taskRepository.findByAuthor(user, pageable).stream()
                .map(task -> mapper.map(task, TaskDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getTasksByExecutor(int executorId, Status status, Pageable pageable) {

        //todo добавить фильтр по статусу!!!

        User user = userRepository.findById(executorId)
                .orElseThrow(() -> new NotFoundException("Невозможно получить список задач. Пользователь с id "
                        + executorId + " не найден"));
        return taskRepository.findByExecutor(user, pageable).stream()
                .map(task -> mapper.map(task, TaskDto.class)).collect(Collectors.toList());
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
        task.addNotes(mapper.map(note, NoteDto.class));
        return mapper.map(taskRepository.save(task), TaskDto.class);
    }
}