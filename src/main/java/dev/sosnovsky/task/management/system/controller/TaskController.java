package dev.sosnovsky.task.management.system.controller;

import com.github.fge.jsonpatch.JsonPatch;
import dev.sosnovsky.task.management.system.dto.CreateNoteDto;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import dev.sosnovsky.task.management.system.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/task")
@SecurityRequirement(name = "JWT")
@Tag(name = "Контроллер для работы с задачами", description = "Позволяет создавать новые задачи, редактировать" +
        "  и удалять существующие, получать задачи по id или списком, менять статус задачи, назначать исполнителя" +
        " и добавлять комментарии к задаче")
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Создание задачи. Доступно всем аутентифицированным пользователям")
    public TaskDto create(@RequestBody @Valid CreateTaskDto createTaskDto, Principal principal) {
        return taskService.create(createTaskDto, principal);
    }

    @PatchMapping("/taskId")
    @Operation(summary = "Обновление задачи. Доступно аутентифицированным пользователям: автору или исполнителю")
    public TaskDto update(@PathVariable @Min(value = 1, message = "id должен быть числом больше нуля") int taskId,
                          @RequestBody JsonPatch jsonPatch, Principal principal) {
        return taskService.update(taskId, jsonPatch, principal);
    }

    @DeleteMapping("/taskId")
    @Operation(summary = "Удаление задачи. Доступно только автору задачи")
    public void delete(@PathVariable @Min(value = 1, message = "id должен быть числом больше нуля") int taskId,
                       Principal principal) {
        taskService.delete(taskId, principal);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Получение задачи по id. Доступно всем аутентифицированным пользователям")
    public TaskDto getTaskById(@PathVariable @Min(value = 1, message = "id должен быть числом больше нуля") int taskId) {
        return taskService.getTaskById(taskId);
    }

    @GetMapping
    @Operation(summary = "Получение списка задач. Доступно всем аутентифицированным пользователям",
            description = "Для получения всех задач, созданных пользователем, необходимо заполнить параметр authorId." +
                    " Для получения всех задач, где пользователь указан исполнителем, необходимо заполнить" +
                    " параметр executorId. Предусмотрена возможность добавления фильтров по полям status и priority." +
                    " При отсутствии всех параметров будут получены все задачи, с учётом условий пагинации.")
    public List<TaskDto> getTaskByFilters(@RequestParam(value = "authorId", required = false) Integer authorId,
                                          @RequestParam(value = "executorId", required = false) Integer executorId,
                                          @RequestParam(value = "status", required = false) Status status,
                                          @RequestParam(value = "priority", required = false) Priority priority,
                                          @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                          @RequestParam(value = "size", required = false, defaultValue = "5") int size) {
        return taskService.getTasksByFilters(authorId, executorId, status, priority, PageRequest.of(page, size));
    }

    @PutMapping("/{taskId}/changeStatus")
    @Operation(summary = "Изменение статуса задачи. Доступно аутентифицированным пользователям: автору или исполнителю")
    public TaskDto changeTaskStatus(
            @PathVariable @Min(value = 1, message = "id должен быть числом больше нуля") int taskId,
            @RequestParam(value = "status") Status status,
            Principal principal) {
        return taskService.changeTaskStatus(taskId, status, principal);
    }

    @PutMapping("/{taskId}/setExecutor")
    @Operation(summary = "Назначение исполнителя задачи. Доступно только автору задачи")
    public TaskDto setExecutor(@PathVariable @Min(value = 1, message = "id должен быть числом больше нуля") int taskId,
                               @RequestParam(value = "executorId") int executorId,
                               Principal principal) {
        return taskService.setExecutor(taskId, executorId, principal);
    }

    @PostMapping("/note")
    @Operation(summary = "Добавление комментария к задачи. Доступно всем аутентифицированным пользователям")
    public TaskDto addNote(@RequestBody @Valid CreateNoteDto createNoteDto, Principal principal) {
        return taskService.addNote(createNoteDto, principal);
    }
}