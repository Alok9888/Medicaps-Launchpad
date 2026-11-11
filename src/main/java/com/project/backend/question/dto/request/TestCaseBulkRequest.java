package com.project.backend.question.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TestCaseBulkRequest(
        @NotEmpty List<TestCaseCreateRequest> testCases
) {}