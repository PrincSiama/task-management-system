package dev.sosnovsky.task.management.system.service;

import com.github.fge.jsonpatch.JsonPatch;
import dev.sosnovsky.task.management.system.dto.CreateNoteDto;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;

public interface TaskService {
    TaskDto create(CreateTaskDto createTaskDto, Principal principal);

    TaskDto update(int taskId, JsonPatch jsonPatch, Principal principal);

    void delete(int taskId, Principal principal);

    TaskDto getTaskById(int taskId);

    List<TaskDto> getTasksByFilters(Integer authorId, Integer executorId,
                                    Status status, Priority priority, Pageable pageable);

    TaskDto changeTaskStatus(int taskId, Status status, Principal principal);

    TaskDto setExecutor(int taskId, int executorId, Principal principal);

    TaskDto addNote(CreateNoteDto createNoteDto, Principal principal);
}