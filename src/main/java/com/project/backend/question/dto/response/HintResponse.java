package com.project.backend.question.dto.response;

public record HintResponse(
        Integer order,
        String content,
        Integer totalHints
) {}