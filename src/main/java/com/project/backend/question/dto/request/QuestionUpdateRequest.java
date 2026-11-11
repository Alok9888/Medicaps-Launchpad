package com.project.backend.question.dto.request;

import com.project.backend.question.domain.Difficulty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record QuestionUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 220) String slug,
        @NotBlank String description,
        @NotNull Difficulty difficulty,
        @NotBlank String constraints,
        @Size(max = 5000) String inputFormat,
        @Size(max = 5000) String outputFormat,
        @Min(100) @Max(10000) Integer timeLimitMs,
        @Min(16384) @Max(524288) Integer memoryLimitKb,
        @Size(max = 20) String comparator,
        BigDecimal floatEpsilon,
        @NotEmpty List<String> topics,
        @NotEmpty List<CompanyRequest> companies,
        @NotEmpty List<TestCaseRequest> testCases,
        List<LanguageRequest> languages,
        List<String> hints,
        @NotNull Long rowVersion
) {
    public record CompanyRequest(
            @NotNull Long id,
            @Min(1) Integer frequency
    ) {}

    public record TestCaseRequest(
            Long id,
            @NotBlank String input,
            @NotBlank String expectedOutput,
            boolean hidden,
            @Min(0) Integer sequenceOrder,
            String explanation
    ) {}

    public record LanguageRequest(
            @NotNull Long languageId,
            String starterCode,
            @NotNull Boolean enabled
    ) {}
}