package com.project.backend.question.dto.response;

public record CompanySummary(
        Long id,
        String name,
        long questionCount
) {}