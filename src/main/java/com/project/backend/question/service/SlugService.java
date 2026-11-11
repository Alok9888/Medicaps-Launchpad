package com.project.backend.question.service;

import com.project.backend.question.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class SlugService {

    private final ProblemRepository problemRepository;
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final int MAX_SLUG_LENGTH = 220;

    public SlugService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            return "untitled-" + System.currentTimeMillis();
        }

        // Normalize unicode, remove accents
        String normalized = Normalizer.normalize(title.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase, replace non-alphanumeric with hyphens
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Truncate if too long
        if (slug.length() > MAX_SLUG_LENGTH - 10) {
            slug = slug.substring(0, MAX_SLUG_LENGTH - 10);
            slug = slug.replaceAll("-+$", "");
        }

        // Ensure uniqueness
        return makeUnique(slug);
    }

    public String makeUnique(String baseSlug) {
        if (!problemRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        int counter = 1;
        String candidate;
        do {
            candidate = baseSlug + "-" + counter;
            counter++;
        } while (problemRepository.existsBySlug(candidate) && counter < 1000);

        return candidate;
    }

    public boolean isValidSlug(String slug) {
        return slug != null && SLUG_PATTERN.matcher(slug).matches();
    }

    public String sanitizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }

        String sanitized = slug.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return sanitized.isEmpty() ? null : sanitized;
    }
}