package com.project.backend.question.dto.response;

public record LanguageSummary(
        Long id,
        String name,
        String fileExtension,
        String starterCode,
        Boolean enabled
) {
    // Convenience constructor for student-facing APIs
    public LanguageSummary(Long id, String name, String fileExtension) {
        this(id, name, fileExtension, null, null);
    }
}