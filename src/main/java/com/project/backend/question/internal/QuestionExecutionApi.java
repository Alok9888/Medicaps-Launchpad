package com.project.backend.question.internal;

import com.project.backend.common.exception.NotFoundException;
import com.project.backend.question.domain.Language;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemLanguage;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.domain.TestCase;
import com.project.backend.question.repository.LanguageRepository;
import com.project.backend.question.repository.ProblemLanguageRepository;
import com.project.backend.question.repository.ProblemRepository;
import com.project.backend.question.repository.TestCaseRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class QuestionExecutionApi {

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProblemLanguageRepository problemLanguageRepository;
    private final LanguageRepository languageRepository;

    public QuestionExecutionApi(
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            ProblemLanguageRepository problemLanguageRepository,
            LanguageRepository languageRepository
    ) {
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.problemLanguageRepository = problemLanguageRepository;
        this.languageRepository = languageRepository;
    }

    @Cacheable(value = "executionQuestion", key = "#questionId", unless = "#result == null")
    public ExecutionQuestion getExecutionQuestion(Long questionId) {
        Problem problem = problemRepository.findByIdAndStatus(questionId, ProblemStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        List<TestCase> testCases = testCaseRepository.findByProblemIdOrderBySequenceOrderAsc(questionId);

        List<ExecutionLanguage> languages = problemLanguageRepository.findByProblemIdAndEnabledTrue(questionId).stream()
                .map(pl -> {
                    Language lang = pl.getLanguage();
                    return new ExecutionLanguage(
                            lang.getId(),
                            lang.getName(),
                            lang.getJudge0LanguageId(),
                            lang.getFileExtension(),
                            pl.getStarterCode()
                    );
                })
                .toList();

        return new ExecutionQuestion(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitKb(),
                problem.getComparator(),
                problem.getFloatEpsilon(),
                testCases.stream().map(this::mapToExecutionTestCase).toList(),
                languages
        );
    }

    @Cacheable(value = "executionTestCases", key = "#questionId", unless = "#result.isEmpty()")
    public List<ExecutionTestCase> getExecutionTestCases(Long questionId) {
        Problem problem = problemRepository.findByIdAndStatus(questionId, ProblemStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        return testCaseRepository.findByProblemIdOrderBySequenceOrderAsc(questionId).stream()
                .map(this::mapToExecutionTestCase)
                .toList();
    }

    @Cacheable(value = "executionLanguages", key = "#questionId", unless = "#result.isEmpty()")
    public List<ExecutionLanguage> getExecutionLanguages(Long questionId) {
        Problem problem = problemRepository.findByIdAndStatus(questionId, ProblemStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        return problemLanguageRepository.findByProblemIdAndEnabledTrue(questionId).stream()
                .map(pl -> {
                    Language lang = pl.getLanguage();
                    return new ExecutionLanguage(
                            lang.getId(),
                            lang.getName(),
                            lang.getJudge0LanguageId(),
                            lang.getFileExtension(),
                            pl.getStarterCode()
                    );
                })
                .toList();
    }

    @Cacheable(value = "languageById", key = "#languageId", unless = "#result == null")
    public Optional<Language> findLanguageById(Long languageId) {
        return languageRepository.findById(languageId);
    }

    @Cacheable(value = "languageByJudge0Id", key = "#judge0LanguageId", unless = "#result == null")
    public Optional<Language> findLanguageByJudge0Id(Integer judge0LanguageId) {
        return languageRepository.findByJudge0LanguageId(judge0LanguageId);
    }

    private ExecutionTestCase mapToExecutionTestCase(TestCase tc) {
        return new ExecutionTestCase(
                tc.getId(),
                tc.getInput(),
                tc.getExpectedOutput(),
                tc.isHidden(),
                tc.getSequenceOrder()
        );
    }

    public record ExecutionQuestion(
            Long id,
            String title,
            String slug,
            Integer timeLimitMs,
            Integer memoryLimitKb,
            String comparator,
            java.math.BigDecimal floatEpsilon,
            List<ExecutionTestCase> testCases,
            List<ExecutionLanguage> languages
    ) {}

    public record ExecutionTestCase(
            Long id,
            String input,
            String expectedOutput,
            boolean hidden,
            Integer sequenceOrder
    ) {}

    public record ExecutionLanguage(
            Long id,
            String name,
            Integer judge0LanguageId,
            String fileExtension,
            String starterCode
    ) {}
}