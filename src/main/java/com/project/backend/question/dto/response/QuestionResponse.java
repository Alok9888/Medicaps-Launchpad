package com.project.backend.question.dto.response;

import com.project.backend.question.domain.Difficulty;
import java.util.List;

public record QuestionResponse(
        Long id,
        String title,
        String slug,
        String description,
        Difficulty difficulty,
        String constraints,
        String inputFormat,
        String outputFormat,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        List<String> topics,
        List<CompanySummary> companies,
        List<SampleTestCaseResponse> sampleTestCases,
        List<LanguageSummary> languages,
        Integer hintCount
) {
    public record CompanySummary(Long id, String name, Integer frequency) {}
    public record LanguageSummary(Long id, String name, String fileExtension, String starterCode, Boolean enabled) {
        public LanguageSummary(Long id, String name, String fileExtension, String starterCode) {
            this(id, name, fileExtension, starterCode, null);
        }
    }
}