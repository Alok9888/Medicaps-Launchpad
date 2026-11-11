package com.project.backend.question.dto.request;

import jakarta.validation.constraints.*;

public record TestCaseCreateRequest(
        @NotBlank String input,
        @NotBlank String expectedOutput,
        boolean hidden,
        @Min(0) Integer sequenceOrder,
        String explanation
) {}