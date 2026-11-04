package com.project.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.dto.request.TaskRequest;
import com.project.backend.model.TaskStatus;
import com.project.backend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        // Guarantee clean test isolation between individual test runs
        taskService.clear();
    }

    @Test
    @DisplayName("GET /api/v1/tasks returns 200 OK with empty array when no tasks exist")
    void getAllTasks_whenNoTasks_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/v1/tasks with valid payload and omitted status defaults to PENDING and returns 201 Created")
    void createTask_withValidDataAndDefaultStatus_shouldReturnCreated() throws Exception {
        TaskRequest request = new TaskRequest("Implement foundation", "Build baseline APIs", null);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title", is("Implement foundation")))
                .andExpect(jsonPath("$.data.description", is("Build baseline APIs")))
                .andExpect(jsonPath("$.data.status", is("PENDING")));
    }

    @Test
    @DisplayName("POST /api/v1/tasks with explicit status returns 201 Created with provided status")
    void createTask_withExplicitStatus_shouldReturnCreated() throws Exception {
        TaskRequest request = new TaskRequest("Review architecture", "Examine sequence diagram", TaskStatus.IN_PROGRESS);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.title", is("Review architecture")))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} returns 200 OK when task exists")
    void getTaskById_whenExists_shouldReturnTask() throws Exception {
        TaskRequest createRequest = new TaskRequest("Existing Task", "Already stored", TaskStatus.COMPLETED);
        var created = taskService.createTask(createRequest);

        mockMvc.perform(get("/api/v1/tasks/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.data.title", is("Existing Task")))
                .andExpect(jsonPath("$.data.description", is("Already stored")))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("POST /api/v1/tasks with blank title returns 400 Bad Request with ErrorResponse")
    void createTask_withBlankTitle_shouldReturnBadRequest() throws Exception {
        String invalidPayload = "{\"title\":\"\", \"description\":\"Invalid task with empty title\"}";

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Title is required")))
                .andExpect(jsonPath("$.path", is("/api/v1/tasks")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/tasks with invalid status string returns 400 Bad Request with ErrorResponse")
    void createTask_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        String invalidStatusPayload = "{\"title\":\"Task with bad status\", \"status\":\"UNKNOWN_STATUS\"}";

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStatusPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Allowed values are: [PENDING, IN_PROGRESS, COMPLETED]")))
                .andExpect(jsonPath("$.path", is("/api/v1/tasks")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/tasks/{id} returns 404 Not Found when ID does not exist")
    void getTaskById_whenNotFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Task with ID 999 not found")))
                .andExpect(jsonPath("$.path", is("/api/v1/tasks/999")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
