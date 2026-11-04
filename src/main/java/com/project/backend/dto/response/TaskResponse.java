package com.project.backend.dto.response;

import com.project.backend.model.Task;
import com.project.backend.model.TaskStatus;

/**
 * DTO for task response payload.
 */
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public static TaskResponse fromModel(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
