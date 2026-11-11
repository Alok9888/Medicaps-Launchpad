package com.project.backend.question.service;

import com.project.backend.common.config.QuestionProperties;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemLanguage;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.domain.TestCase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PublishCheckService {

    private final QuestionProperties properties;

    public PublishCheckService(QuestionProperties properties) {
        this.properties = properties;
    }

    public PublishCheckResult check(Problem problem) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> info = new ArrayList<>();

        // === Required Fields ===
        checkRequiredField(problem.getTitle(), "Title", errors);
        checkRequiredField(problem.getSlug(), "Slug", errors);
        checkRequiredField(problem.getDescription(), "Description", errors);
        checkRequiredField(problem.getProblemConstraints(), "Constraints", errors);
        checkRequiredField(problem.getInputFormat(), "Input format", errors);
        checkRequiredField(problem.getOutputFormat(), "Output format", errors);

        if (problem.getDifficulty() == null) {
            errors.add("Difficulty is required");
        }

        if (problem.getTimeLimitMs() == null) {
            errors.add("Time limit is required");
        } else if (problem.getTimeLimitMs() < properties.getLimits().getMinTimeMs()) {
            errors.add("Time limit must be at least " + properties.getLimits().getMinTimeMs() + "ms");
        } else if (problem.getTimeLimitMs() > properties.getLimits().getMaxTimeMs()) {
            errors.add("Time limit must not exceed " + properties.getLimits().getMaxTimeMs() + "ms");
        }

        if (problem.getMemoryLimitKb() == null) {
            errors.add("Memory limit is required");
        } else if (problem.getMemoryLimitKb() < properties.getLimits().getMinMemoryKb()) {
            errors.add("Memory limit must be at least " + properties.getLimits().getMinMemoryKb() + "KB");
        } else if (problem.getMemoryLimitKb() > properties.getLimits().getMaxMemoryKb()) {
            errors.add("Memory limit must not exceed " + properties.getLimits().getMaxMemoryKb() + "KB");
        }

        if (problem.getComparator() == null || problem.getComparator().isBlank()) {
            errors.add("Comparator is required");
        }

        // === Topics ===
        if (problem.getTopics().isEmpty()) {
            errors.add("At least one topic is required");
        } else {
            info.add("Topics: " + problem.getTopics().size());
        }

        // === Companies ===
        if (problem.getProblemCompanies().isEmpty()) {
            errors.add("At least one company is required");
        } else {
            info.add("Companies: " + problem.getProblemCompanies().size());
        }

        // === Test Cases ===
        long sampleCount = problem.getTestCases().stream().filter(tc -> !tc.isHidden()).count();
        long hiddenCount = problem.getTestCases().stream().filter(TestCase::isHidden).count();

        if (sampleCount < properties.getPublish().getMinSampleCases()) {
            errors.add("At least " + properties.getPublish().getMinSampleCases() + " sample test case(s) required (found " + sampleCount + ")");
        }
        if (hiddenCount < properties.getPublish().getMinHiddenCases()) {
            errors.add("At least " + properties.getPublish().getMinHiddenCases() + " hidden test case(s) required (found " + hiddenCount + ")");
        }

        // Test case quality checks
        validateTestCases(problem.getTestCases(), errors, warnings);

        // === Languages ===
        if (problem.getProblemLanguages().isEmpty()) {
            errors.add("At least one language configuration required");
        } else {
            long enabledCount = problem.getProblemLanguages().stream().filter(ProblemLanguage::getEnabled).count();
            if (enabledCount == 0) {
                errors.add("At least one enabled language required");
            } else {
                info.add("Enabled languages: " + enabledCount);
            }
        }

        // === Hints (optional but recommended) ===
        if (problem.getHints().isEmpty()) {
            warnings.add("No hints configured - consider adding progressive hints");
        } else {
            info.add("Hints: " + problem.getHints().size());
        }

        // === Slug format ===
        if (problem.getSlug() != null && !isValidSlug(problem.getSlug())) {
            warnings.add("Slug should be lowercase with hyphens only (current: " + problem.getSlug() + ")");
        }

        // === Content quality ===
        validateContentQuality(problem, warnings, info);

        boolean canPublish = errors.isEmpty() && problem.getStatus() != ProblemStatus.PUBLISHED;

        return new PublishCheckResult(canPublish, errors, warnings, info);
    }

    private void checkRequiredField(String value, String fieldName, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(fieldName + " is required");
        }
    }

    private void validateTestCases(List<TestCase> testCases, List<String> errors, List<String> warnings) {
        if (testCases.isEmpty()) return;

        // Check sequence order continuity
        List<Integer> seqOrders = testCases.stream()
                .map(TestCase::getSequenceOrder)
                .sorted()
                .toList();

        for (int i = 0; i < seqOrders.size(); i++) {
            if (!seqOrders.get(i).equals(i)) {
                warnings.add("Test case sequence order has gaps (expected 0.." + (seqOrders.size() - 1) + ")");
                break;
            }
        }

        // Check for duplicate inputs
        testCases.stream()
                .map(TestCase::getInput)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.groupingBy(java.util.function.Function.identity(), java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .findFirst()
                .ifPresent(e -> warnings.add("Duplicate test case input detected: " + e.getKey().substring(0, Math.min(50, e.getKey().length())) + "..."));

        // Check hidden cases have explanations
        long hiddenWithoutExplanation = testCases.stream()
                .filter(TestCase::isHidden)
                .filter(tc -> tc.getExplanation() == null || tc.getExplanation().isBlank())
                .count();
        if (hiddenWithoutExplanation > 0) {
            warnings.add(hiddenWithoutExplanation + " hidden test case(s) missing explanation");
        }
    }

    private void validateContentQuality(Problem problem, List<String> warnings, List<String> info) {
        // Description length
        if (problem.getDescription() != null) {
            int wordCount = problem.getDescription().split("\\s+").length;
            if (wordCount < 50) {
                warnings.add("Description seems short (" + wordCount + " words) - consider adding more detail");
            } else if (wordCount > 5000) {
                warnings.add("Description is very long (" + wordCount + " words) - consider condensing");
            }
        }

        // Constraints length
        if (problem.getProblemConstraints() != null) {
            int constraintLines = problem.getProblemConstraints().split("\\n").length;
            if (constraintLines < 3) {
                warnings.add("Constraints seem minimal - consider adding more boundary conditions");
            }
        }

        // Time/memory ratio check
        if (problem.getTimeLimitMs() != null && problem.getMemoryLimitKb() != null) {
            double ratio = (double) problem.getMemoryLimitKb() / problem.getTimeLimitMs();
            if (ratio > 500) {
                warnings.add("High memory-to-time ratio - verify limits are appropriate");
            }
        }
    }

    private boolean isValidSlug(String slug) {
        return Pattern.matches("^[a-z0-9]+(-[a-z0-9]+)*$", slug);
    }

    public record PublishCheckResult(
            boolean canPublish,
            List<String> errors,
            List<String> warnings,
            List<String> info
    ) {}
}