package com.project.backend.question.dto.request;

import jakarta.validation.constraints.*;

public record LanguageCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Integer judge0LanguageId,
        @NotBlank @Size(max = 20) String fileExtension,
        @NotNull Boolean isActive
) {}