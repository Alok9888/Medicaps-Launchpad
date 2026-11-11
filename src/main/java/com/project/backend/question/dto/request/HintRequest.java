package com.project.backend.question.dto.request;

import jakarta.validation.constraints.*;

public record HintRequest(
        @Min(1) Integer order,
        @NotBlank String content
) {}