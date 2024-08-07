package dev.sosnovsky.task.management.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sosnovsky.task.management.system.dto.CreateNoteDto;
import dev.sosnovsky.task.management.system.dto.CreateTaskDto;
import dev.sosnovsky.task.management.system.dto.TaskDto;
import dev.sosnovsky.task.management.system.exception.NotFoundException;
import dev.sosnovsky.task.management.system.exception.PermissionDeniedException;
import dev.sosnovsky.task.management.system.model.Priority;
import dev.sosnovsky.task.management.system.model.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext
    @WithMockUser(username = "jjameson@user.com")
    @DisplayName("Корректное создание задачи аутентифицированным пользователем")
    public void createTaskByAuthUserTest() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 1", "description task 1", Priority.HIGH);

        mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "user@user.com")
    @DisplayName("Создание задачи не существующим пользователем")
    public void createTaskByNonAuthUserTest() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 2", "description task 2", Priority.HIGH);

        mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jjameson@user.com")
    @DisplayName("Корректное удаление задачи автором")
    public void deleteTaskByAuthorTest() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 3", "description task 3");

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TaskDto.class);

        assertNotNull(taskDto);

        mockMvc.perform(delete("/task/{id}", taskDto.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jjameson@user.com")
    @DisplayName("Получение задачи по id аутентифицированным пользователем")
    public void getTaskByIdByAuthUserTest() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 4", "description task 4");

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(csrf())
                        .with(user("asanta@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TaskDto.class);

        assertNotNull(taskDto);

        mockMvc.perform(get("/task/{id}", taskDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskDto.getId()));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jwalker@user.com")
    @DisplayName("Получение списка задач с фильтрами аутентифицированным пользователем")
    public void getTaskListWithFiltersByAuthUserTest() throws Exception {
        CreateTaskDto createTaskDto1 = new CreateTaskDto("test task 5", "description task 5");
        CreateTaskDto createTaskDto2 = new CreateTaskDto("test task 6", "description task 6", Priority.HIGH);

        MvcResult mvcResult1 = mockMvc.perform(post("/task")
                        .with(csrf())
                        .with(user("asanta@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto1 = objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), TaskDto.class);

        MvcResult mvcResult2 = mockMvc.perform(post("/task")
                        .with(csrf())
                        .with(user("jdaniels@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto2 = objectMapper.readValue(mvcResult2.getResponse().getContentAsString(), TaskDto.class);


        mockMvc.perform(get("/task")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(taskDto2))));

        mockMvc.perform(get("/task"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(taskDto1, taskDto2))));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jwalker@user.com")
    @DisplayName("Изменение статуса задачи автором")
    public void changeTaskStatusByAuthor() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 7", "description task 7");

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TaskDto.class);

        assertNotNull(taskDto);

        MvcResult changeMvcResult = mockMvc.perform(put("/task/{id}/changeStatus", taskDto.getId())
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto changeTaskDto = objectMapper.readValue(changeMvcResult.getResponse().getContentAsString(), TaskDto.class);

        assertNotNull(changeTaskDto);
        assertEquals(Status.IN_PROGRESS, changeTaskDto.getStatus());
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jchivas@user.com")
    @DisplayName("Изменение статуса задачи пользователем")
    public void changeTaskStatusByUserTest() throws Exception {
        int executorId = 5;
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 8", "description task 8", executorId);

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .with(csrf())
                        .with(user("jdaniels@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TaskDto.class);

        assertNotNull(taskDto);

        mockMvc.perform(put("/task/{taskId}/changeStatus", taskDto.getId())
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(PermissionDeniedException.class, result.getResolvedException()));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jchivas@user.com")
    @DisplayName("Назначение исполнителя автором задачи")
    public void setExecutorByAuthorTest() throws Exception {
        int executorId = 5;
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 9", "description task 9");

        MvcResult mvcResult = mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), TaskDto.class);

        mockMvc.perform(put("/task/{taskId}/setExecutor", taskDto.getId())
                        .param("executorId", String.valueOf(executorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.executor.id").value(executorId));
    }

    @Test
    @DirtiesContext
    @WithMockUser(username = "jchivas@user.com")
    @DisplayName("Добавление заметки к существующей задаче")
    public void addNoteTest() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto("test task 10", "description task 10");

        MvcResult taskMvcResult = mockMvc.perform(post("/task")
                        .with(csrf())
                        .with(user("jdaniels@user.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        TaskDto taskDto = objectMapper.readValue(taskMvcResult.getResponse().getContentAsString(), TaskDto.class);

        CreateNoteDto createNoteDto = new CreateNoteDto(taskDto.getId(), "note for task 10");

        mockMvc.perform(post("/task/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNoteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.notes").isNotEmpty());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Получение задачи по id не аутентифицированным пользователем")
    public void unauthorizedAccessTest() throws Exception {
        mockMvc.perform(get("/task/1"))
                .andExpect(status().is3xxRedirection());
    }
}