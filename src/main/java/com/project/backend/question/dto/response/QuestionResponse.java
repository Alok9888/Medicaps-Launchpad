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
        List<String> topics,
        List<SampleTestCaseResponse> sampleTestCases
) {
}