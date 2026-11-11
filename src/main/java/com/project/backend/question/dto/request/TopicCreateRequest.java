package com.project.backend.question.dto.request;

import jakarta.validation.constraints.*;

public record TopicCreateRequest(
        @NotBlank @Size(max = 100) String name
) {}