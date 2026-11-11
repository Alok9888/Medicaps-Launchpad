package com.project.backend.question.service;

import com.project.backend.common.exception.ValidationException;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EditRulesService {

    // Fields that can NEVER be changed after creation
    private static final Set<String> IMMUTABLE_FIELDS = Set.of(
            "id", "slug", "createdAt", "createdBy", "publishedAt", "rowVersion"
    );

    // Fields that can ONLY be changed in DRAFT status
    private static final Set<String> DRAFT_ONLY_FIELDS = Set.of(
            "title", "description", "difficulty", "problemConstraints",
            "inputFormat", "outputFormat", "timeLimitMs", "memoryLimitKb",
            "comparator", "floatEpsilon", "topics", "companies",
            "testCases", "languages", "hints"
    );

    // Fields that can be changed in DRAFT or ARCHIVED (but not PUBLISHED)
    private static final Set<String> NOT_PUBLISHED_FIELDS = Set.of(
            "title", "description", "difficulty", "problemConstraints",
            "inputFormat", "outputFormat", "timeLimitMs", "memoryLimitKb",
            "comparator", "floatEpsilon", "topics", "companies",
            "testCases", "languages", "hints"
    );

    // Fields that can ALWAYS be changed (with proper permissions)
    private static final Set<String> ALWAYS_EDITABLE = Set.of(
            "updatedBy"
    );

    // Status transition rules
    private static final Set<ProblemStatus> PUBLISHABLE_FROM = EnumSet.of(
            ProblemStatus.DRAFT, ProblemStatus.ARCHIVED
    );

    private static final Set<ProblemStatus> ARCHIVABLE_FROM = EnumSet.of(
            ProblemStatus.PUBLISHED
    );

    private static final Set<ProblemStatus> UNARCHIVABLE_FROM = EnumSet.of(
            ProblemStatus.ARCHIVED
    );

    private static final Set<ProblemStatus> DELETABLE_FROM = EnumSet.of(
            ProblemStatus.DRAFT
    );

    private static final Set<ProblemStatus> CLONABLE_FROM = EnumSet.of(
            ProblemStatus.DRAFT, ProblemStatus.PUBLISHED, ProblemStatus.ARCHIVED
    );

    public void validateEditAllowed(Problem problem, Set<String> changedFields) {
        ProblemStatus status = problem.getStatus();

        for (String field : changedFields) {
            if (IMMUTABLE_FIELDS.contains(field)) {
                throw new ValidationException("IMMUTABLE_FIELD", "Field '" + field + "' cannot be modified");
            }

            if (status == ProblemStatus.PUBLISHED && DRAFT_ONLY_FIELDS.contains(field)) {
                throw new ValidationException("EDIT_NOT_ALLOWED",
                        "Cannot modify '" + field + "' on a published question. Archive first to make changes.");
            }

            if (status == ProblemStatus.ARCHIVED && !NOT_PUBLISHED_FIELDS.contains(field) && !ALWAYS_EDITABLE.contains(field)) {
                throw new ValidationException("EDIT_NOT_ALLOWED",
                        "Cannot modify '" + field + "' on an archived question. Unarchive first to make changes.");
            }
        }
    }

    public void validateCanPublish(Problem problem) {
        if (!PUBLISHABLE_FROM.contains(problem.getStatus())) {
            throw new ValidationException("ILLEGAL_STATUS_TRANSITION",
                    "Cannot publish from status: " + problem.getStatus() + ". Must be DRAFT or ARCHIVED.");
        }
    }

    public void validateCanArchive(Problem problem) {
        if (!ARCHIVABLE_FROM.contains(problem.getStatus())) {
            throw new ValidationException("ILLEGAL_STATUS_TRANSITION",
                    "Cannot archive from status: " + problem.getStatus() + ". Must be PUBLISHED.");
        }
    }

    public void validateCanUnarchive(Problem problem) {
        if (!UNARCHIVABLE_FROM.contains(problem.getStatus())) {
            throw new ValidationException("ILLEGAL_STATUS_TRANSITION",
                    "Cannot unarchive from status: " + problem.getStatus() + ". Must be ARCHIVED.");
        }
    }

    public void validateCanDelete(Problem problem) {
        if (!DELETABLE_FROM.contains(problem.getStatus())) {
            throw new ValidationException("ILLEGAL_STATUS_TRANSITION",
                    "Cannot delete from status: " + problem.getStatus() + ". Only DRAFT questions can be deleted.");
        }
    }

    public void validateCanClone(Problem problem) {
        if (!CLONABLE_FROM.contains(problem.getStatus())) {
            throw new ValidationException("ILLEGAL_STATUS_TRANSITION",
                    "Cannot clone from status: " + problem.getStatus());
        }
    }

    public Set<String> getEditableFields(ProblemStatus status) {
        switch (status) {
            case DRAFT: {
                Set<String> allFields = new HashSet<>(DRAFT_ONLY_FIELDS);
                allFields.addAll(ALWAYS_EDITABLE);
                return allFields;
            }
            case ARCHIVED: {
                Set<String> archivedEditable = new HashSet<>(NOT_PUBLISHED_FIELDS);
                archivedEditable.addAll(ALWAYS_EDITABLE);
                return archivedEditable;
            }
            case PUBLISHED:
                return new HashSet<>(ALWAYS_EDITABLE);
            default:
                return Set.of();
        }
    }

    public boolean isFieldEditable(String field, ProblemStatus status) {
        if (IMMUTABLE_FIELDS.contains(field)) {
            return false;
        }
        if (ALWAYS_EDITABLE.contains(field)) {
            return true;
        }
        if (status == ProblemStatus.DRAFT) {
            return true;
        }
        if (status == ProblemStatus.ARCHIVED) {
            return NOT_PUBLISHED_FIELDS.contains(field);
        }
        // PUBLISHED - only always editable
        return false;
    }

    public record FieldEditRule(
            String field,
            boolean editableInDraft,
            boolean editableInPublished,
            boolean editableInArchived,
            String reason
    ) {}

    public List<FieldEditRule> getAllFieldRules() {
        return List.of(
                new FieldEditRule("id", false, false, false, "Primary key - immutable"),
                new FieldEditRule("slug", false, false, false, "Unique identifier - immutable after creation"),
                new FieldEditRule("title", true, false, true, "Core content"),
                new FieldEditRule("description", true, false, true, "Core content"),
                new FieldEditRule("difficulty", true, false, true, "Core content"),
                new FieldEditRule("problemConstraints", true, false, true, "Core content"),
                new FieldEditRule("inputFormat", true, false, true, "Core content"),
                new FieldEditRule("outputFormat", true, false, true, "Core content"),
                new FieldEditRule("timeLimitMs", true, false, true, "Execution config"),
                new FieldEditRule("memoryLimitKb", true, false, true, "Execution config"),
                new FieldEditRule("comparator", true, false, true, "Execution config"),
                new FieldEditRule("floatEpsilon", true, false, true, "Execution config"),
                new FieldEditRule("topics", true, false, true, "Categorization"),
                new FieldEditRule("companies", true, false, true, "Categorization"),
                new FieldEditRule("testCases", true, false, true, "Test data"),
                new FieldEditRule("languages", true, false, true, "Language config"),
                new FieldEditRule("hints", true, false, true, "Hints"),
                new FieldEditRule("status", false, false, false, "Controlled via publish/archive/unarchive actions"),
                new FieldEditRule("contentRevision", false, false, false, "Auto-incremented on edits"),
                new FieldEditRule("publishedAt", false, false, false, "Set on first publish"),
                new FieldEditRule("createdBy", false, false, false, "Immutable audit field"),
                new FieldEditRule("updatedBy", true, true, true, "Audit field"),
                new FieldEditRule("createdAt", false, false, false, "Immutable audit field"),
                new FieldEditRule("updatedAt", false, false, false, "Auto-updated"),
                new FieldEditRule("rowVersion", false, false, false, "Optimistic locking - auto-managed")
        );
    }
}