package com.project.backend.question.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TestCaseReorderRequest(
        @NotEmpty List<ReorderItem> items
) {
    public record ReorderItem(
            @NotNull Long id,
            @Min(0) Integer sequenceOrder
    ) {}
}