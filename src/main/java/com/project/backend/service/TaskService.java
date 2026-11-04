package com.project.backend.service;

import com.project.backend.dto.request.TaskRequest;
import com.project.backend.dto.response.TaskResponse;
import com.project.backend.exception.ResourceNotFoundException;
import com.project.backend.model.Task;
import com.project.backend.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service managing Task business logic and in-memory persistence.
 */
@Service
public class TaskService {

    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    /**
     * Retrieve all tasks ordered by ID.
     */
    public List<TaskResponse> getAllTasks() {
        return tasks.values().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(TaskResponse::fromModel)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single task by ID.
     *
     * @param id Task ID
     * @return TaskResponse
     * @throws ResourceNotFoundException if no task exists with the given ID
     */
    public TaskResponse getTaskById(Long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task with ID " + id + " not found");
        }
        return TaskResponse.fromModel(task);
    }

    /**
     * Create a new task.
     * Defaults status to PENDING if omitted in the request.
     *
     * @param request Task creation payload
     * @return Created TaskResponse
     */
    public TaskResponse createTask(TaskRequest request) {
        Long newId = idSequence.incrementAndGet();
        TaskStatus status = request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING;

        Task task = new Task(newId, request.getTitle(), request.getDescription(), status);
        tasks.put(newId, task);

        return TaskResponse.fromModel(task);
    }

    /**
     * Resets the in-memory store and sequence counter for test isolation.
     * This method is intended strictly for test lifecycle cleanup and is
     * never exposed via any HTTP controller.
     */
    public void clear() {
        tasks.clear();
        idSequence.set(0);
    }
}
