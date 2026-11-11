package com.project.backend.question.dto.request;

import jakarta.validation.constraints.*;

public record CompanyCreateRequest(
        @NotBlank @Size(max = 200) String name
) {}