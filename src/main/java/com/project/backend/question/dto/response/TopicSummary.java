package com.project.backend.question.dto.response;

public record TopicSummary(
        Long id,
        String name,
        long questionCount
) {}