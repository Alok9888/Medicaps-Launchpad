package com.project.backend.dto.response;

import com.project.backend.model.Difficulty;
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