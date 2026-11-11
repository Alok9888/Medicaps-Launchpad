package com.project.backend.question.dto.request;

import jakarta.validation.constraints.*;

public record LanguageRequest(
        @NotNull Long languageId,
        String starterCode,
        @NotNull Boolean enabled
) {}