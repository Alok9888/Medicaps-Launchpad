package com.project.backend.question.internal;

import com.project.backend.common.exception.NotFoundException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/questions")
@SecurityRequirement(name = "internal-key")
public class InternalExecutionController {

    private final QuestionExecutionApi executionApi;

    public InternalExecutionController(QuestionExecutionApi executionApi) {
        this.executionApi = executionApi;
    }

    // GET /api/v1/internal/questions/{id}/execution?samplesOnly=true
    @GetMapping("/{id}/execution")
    public ResponseEntity<QuestionExecutionApi.ExecutionQuestion> getExecutionQuestion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean samplesOnly
    ) {
        QuestionExecutionApi.ExecutionQuestion question = executionApi.getExecutionQuestion(id);
        if (samplesOnly) {
            List<QuestionExecutionApi.ExecutionTestCase> filtered = question.testCases().stream()
                    .filter(tc -> !tc.hidden())
                    .toList();
            question = new QuestionExecutionApi.ExecutionQuestion(
                    question.id(),
                    question.title(),
                    question.slug(),
                    question.timeLimitMs(),
                    question.memoryLimitKb(),
                    question.comparator(),
                    question.floatEpsilon(),
                    filtered,
                    question.languages()
            );
        }
        return ResponseEntity.ok(question);
    }

    // GET /api/v1/internal/questions/{id}/test-cases?samplesOnly=true
    @GetMapping("/{id}/test-cases")
    public ResponseEntity<List<QuestionExecutionApi.ExecutionTestCase>> getExecutionTestCases(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean samplesOnly
    ) {
        List<QuestionExecutionApi.ExecutionTestCase> testCases = executionApi.getExecutionTestCases(id);
        if (samplesOnly) {
            testCases = testCases.stream()
                    .filter(tc -> !tc.hidden())
                    .toList();
        }
        return ResponseEntity.ok(testCases);
    }

    // GET /api/v1/internal/questions/{id}/languages
    @GetMapping("/{id}/languages")
    public ResponseEntity<List<QuestionExecutionApi.ExecutionLanguage>> getExecutionLanguages(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(executionApi.getExecutionLanguages(id));
    }
}
