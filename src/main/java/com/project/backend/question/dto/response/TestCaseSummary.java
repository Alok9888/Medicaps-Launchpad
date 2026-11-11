package com.project.backend.question.dto.response;

public record TestCaseSummary(
        Long id,
        String input,
        String expectedOutput,
        boolean hidden,
        Integer sequenceOrder,
        String explanation
) {}