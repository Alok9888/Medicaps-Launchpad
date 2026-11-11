package com.project.backend.question.service;

import com.project.backend.common.config.QuestionProperties;
import com.project.backend.common.exception.ConflictException;
import com.project.backend.common.exception.NotFoundException;
import com.project.backend.common.exception.ValidationException;
import com.project.backend.question.domain.*;
import com.project.backend.question.dto.request.*;
import com.project.backend.question.dto.response.AdminQuestionResponse;
import com.project.backend.question.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminQuestionService {

    private final ProblemRepository problemRepository;
    private final TopicRepository topicRepository;
    private final CompanyRepository companyRepository;
    private final LanguageRepository languageRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProblemHintRepository problemHintRepository;
    private final ProblemLanguageRepository problemLanguageRepository;
    private final ProblemCompanyRepository problemCompanyRepository;
    private final QuestionProperties properties;
    private final EntityManager entityManager;

    @Autowired
    public AdminQuestionService(
            ProblemRepository problemRepository,
            TopicRepository topicRepository,
            CompanyRepository companyRepository,
            LanguageRepository languageRepository,
            TestCaseRepository testCaseRepository,
            ProblemHintRepository problemHintRepository,
            ProblemLanguageRepository problemLanguageRepository,
            ProblemCompanyRepository problemCompanyRepository,
            QuestionProperties properties,
            EntityManager entityManager
    ) {
        this.problemRepository = problemRepository;
        this.topicRepository = topicRepository;
        this.companyRepository = companyRepository;
        this.languageRepository = languageRepository;
        this.testCaseRepository = testCaseRepository;
        this.problemHintRepository = problemHintRepository;
        this.problemLanguageRepository = problemLanguageRepository;
        this.problemCompanyRepository = problemCompanyRepository;
        this.properties = properties;
        this.entityManager = entityManager;
    }

    @Transactional
    public AdminQuestionResponse createQuestion(QuestionCreateRequest request, Long createdBy) {
        // Validate slug uniqueness
        if (problemRepository.existsBySlug(request.slug())) {
            throw new ConflictException("DUPLICATE_SLUG", "A question with this slug already exists");
        }

        // Validate and get topics
        Set<Topic> topics = request.topics().stream()
                .map(name -> topicRepository.findByName(name)
                        .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "Topic not found: " + name)))
                .collect(Collectors.toCollection(HashSet::new));

        // Validate and get companies
        List<ProblemCompany> problemCompanies = request.companies().stream()
                .map(cr -> {
                    Company company = companyRepository.findById(cr.id())
                            .orElseThrow(() -> new NotFoundException("COMPANY_NOT_FOUND", "Company not found: " + cr.id()));
                    return new ProblemCompany(null, company, cr.frequency());
                })
                .toList();

        // Create problem
        Problem problem = new Problem(
                request.title(),
                request.slug(),
                request.description(),
                request.difficulty(),
                request.constraints()
        );
        problem.setInputFormat(request.inputFormat());
        problem.setOutputFormat(request.outputFormat());
        problem.setTimeLimitMs(request.timeLimitMs());
        problem.setMemoryLimitKb(request.memoryLimitKb());
        problem.setComparator(request.comparator() != null ? request.comparator() : "TRIM_LINES");
        problem.setFloatEpsilon(request.floatEpsilon());
        problem.setCreatedBy(createdBy);
        problem.setUpdatedBy(createdBy);

        topics.forEach(problem::addTopic);
        problemCompanies.forEach(problem::addProblemCompany);

        // Add test cases
        for (int i = 0; i < request.testCases().size(); i++) {
            QuestionCreateRequest.TestCaseRequest tc = request.testCases().get(i);
            TestCase testCase = new TestCase(
                    tc.input(),
                    tc.expectedOutput(),
                    tc.hidden(),
                    tc.sequenceOrder() != null ? tc.sequenceOrder() : i
            );
            testCase.setExplanation(tc.explanation());
            problem.addTestCase(testCase);
        }

        // Add languages
        if (request.languages() != null) {
            for (QuestionCreateRequest.LanguageRequest lr : request.languages()) {
                Language language = languageRepository.findById(lr.languageId())
                        .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not found: " + lr.languageId()));
                ProblemLanguage pl = new ProblemLanguage(problem, language, lr.starterCode(), lr.enabled());
                problem.addProblemLanguage(pl);
            }
        }

        // Add hints
        if (request.hints() != null) {
            for (int i = 0; i < request.hints().size(); i++) {
                ProblemHint hint = new ProblemHint(problem, i + 1, request.hints().get(i));
                problem.addHint(hint);
            }
        }

        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse updateQuestion(Long id, QuestionUpdateRequest request, Long updatedBy) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        // Optimistic locking check
        if (!problem.getRowVersion().equals(request.rowVersion())) {
            throw new ConflictException("STALE_VERSION", "Question was modified by another user");
        }

        // Validate slug uniqueness (excluding current)
        if (!problem.getSlug().equals(request.slug()) && problemRepository.existsBySlug(request.slug())) {
            throw new ConflictException("DUPLICATE_SLUG", "A question with this slug already exists");
        }

        // Update basic fields
        problem.setTitle(request.title());
        problem.setSlug(request.slug());
        problem.setDescription(request.description());
        problem.setDifficulty(request.difficulty());
        problem.setProblemConstraints(request.constraints());
        problem.setInputFormat(request.inputFormat());
        problem.setOutputFormat(request.outputFormat());
        problem.setTimeLimitMs(request.timeLimitMs());
        problem.setMemoryLimitKb(request.memoryLimitKb());
        problem.setComparator(request.comparator() != null ? request.comparator() : "TRIM_LINES");
        problem.setFloatEpsilon(request.floatEpsilon());
        problem.setUpdatedBy(updatedBy);
        problem.incrementContentRevision();

        // Update topics
        problem.getTopics().clear();
        Set<Topic> topics = request.topics().stream()
                .map(name -> topicRepository.findByName(name)
                        .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "Topic not found: " + name)))
                .collect(Collectors.toCollection(HashSet::new));
        topics.forEach(problem::addTopic);

        // Update companies
        problem.getProblemCompanies().clear();
        List<ProblemCompany> problemCompanies = request.companies().stream()
                .map(cr -> {
                    Company company = companyRepository.findById(cr.id())
                            .orElseThrow(() -> new NotFoundException("COMPANY_NOT_FOUND", "Company not found: " + cr.id()));
                    return new ProblemCompany(problem, company, cr.frequency());
                })
                .toList();
        problemCompanies.forEach(problem::addProblemCompany);

        // Update test cases - replace all
        problem.getTestCases().clear();
        for (QuestionUpdateRequest.TestCaseRequest tc : request.testCases()) {
            TestCase testCase;
            if (tc.id() != null) {
                testCase = testCaseRepository.findById(tc.id())
                        .orElseThrow(() -> new NotFoundException("TEST_CASE_NOT_FOUND", "Test case not found: " + tc.id()));
                if (!testCase.getProblem().getId().equals(id)) {
                    throw new ValidationException("TEST_CASE_MISMATCH", "Test case does not belong to this question");
                }
            } else {
                testCase = new TestCase(
                        tc.input(),
                        tc.expectedOutput(),
                        tc.hidden(),
                        tc.sequenceOrder()
                );
                problem.addTestCase(testCase);
            }
            testCase.setInput(tc.input());
            testCase.setExpectedOutput(tc.expectedOutput());
            testCase.setHidden(tc.hidden());
            testCase.setSequenceOrder(tc.sequenceOrder());
            testCase.setExplanation(tc.explanation());
        }

        // Update languages
        problem.getProblemLanguages().clear();
        if (request.languages() != null) {
            for (QuestionUpdateRequest.LanguageRequest lr : request.languages()) {
                Language language = languageRepository.findById(lr.languageId())
                        .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not found: " + lr.languageId()));
                ProblemLanguage pl = new ProblemLanguage(problem, language, lr.starterCode(), lr.enabled());
                problem.addProblemLanguage(pl);
            }
        }

        // Update hints
        problem.getHints().clear();
        if (request.hints() != null) {
            for (int i = 0; i < request.hints().size(); i++) {
                ProblemHint hint = new ProblemHint(problem, i + 1, request.hints().get(i));
                problem.addHint(hint);
            }
        }

        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() != ProblemStatus.DRAFT) {
            throw new ConflictException("ILLEGAL_STATUS_TRANSITION", "Only DRAFT questions can be deleted");
        }

        problemRepository.delete(problem);
    }

    @Transactional
    public AdminQuestionResponse publishQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() != ProblemStatus.DRAFT && problem.getStatus() != ProblemStatus.ARCHIVED) {
            throw new ConflictException("ILLEGAL_STATUS_TRANSITION", "Cannot publish from status: " + problem.getStatus());
        }

        validatePublishReadiness(problem);

        problem.publish();
        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse archiveQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() != ProblemStatus.PUBLISHED) {
            throw new ConflictException("ILLEGAL_STATUS_TRANSITION", "Cannot archive from status: " + problem.getStatus());
        }

        problem.archive();
        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse unarchiveQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() != ProblemStatus.ARCHIVED) {
            throw new ConflictException("ILLEGAL_STATUS_TRANSITION", "Cannot unarchive from status: " + problem.getStatus());
        }

        problem.unarchive();
        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse cloneQuestion(Long id, Long createdBy) {
        Problem original = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        Problem clone = new Problem(
                original.getTitle() + " (Copy)",
                original.getSlug() + "-copy-" + System.currentTimeMillis(),
                original.getDescription(),
                original.getDifficulty(),
                original.getProblemConstraints()
        );
        clone.setInputFormat(original.getInputFormat());
        clone.setOutputFormat(original.getOutputFormat());
        clone.setTimeLimitMs(original.getTimeLimitMs());
        clone.setMemoryLimitKb(original.getMemoryLimitKb());
        clone.setComparator(original.getComparator());
        clone.setFloatEpsilon(original.getFloatEpsilon());
        clone.setCreatedBy(createdBy);
        clone.setUpdatedBy(createdBy);
        clone.setStatus(ProblemStatus.DRAFT);

        // Copy topics
        original.getTopics().forEach(clone::addTopic);

        // Copy companies
        for (ProblemCompany pc : original.getProblemCompanies()) {
            clone.addProblemCompany(new ProblemCompany(clone, pc.getCompany(), pc.getFrequency()));
        }

        // Copy test cases
        for (TestCase tc : original.getTestCases()) {
            TestCase newTc = new TestCase(
                    tc.getInput(),
                    tc.getExpectedOutput(),
                    tc.isHidden(),
                    tc.getSequenceOrder()
            );
            newTc.setExplanation(tc.getExplanation());
            clone.addTestCase(newTc);
        }

        // Copy languages
        for (ProblemLanguage pl : original.getProblemLanguages()) {
            ProblemLanguage newPl = new ProblemLanguage(clone, pl.getLanguage(), pl.getStarterCode(), pl.getEnabled());
            clone.addProblemLanguage(newPl);
        }

        // Copy hints
        for (ProblemHint hint : original.getHints()) {
            clone.addHint(new ProblemHint(clone, hint.getHintOrder(), hint.getContent()));
        }

        Problem saved = problemRepository.save(clone);
        return mapToAdminResponse(saved);
    }

    public AdminQuestionResponse getAdminQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
        return mapToAdminResponse(problem);
    }

    public Page<AdminQuestionResponse> getAdminQuestions(Pageable pageable) {
        return problemRepository.findAll(pageable).map(this::mapToAdminResponse);
    }

    public PublishCheckResponse getPublishCheck(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Required fields
        if (problem.getTitle() == null || problem.getTitle().isBlank()) errors.add("Title is required");
        if (problem.getSlug() == null || problem.getSlug().isBlank()) errors.add("Slug is required");
        if (problem.getDescription() == null || problem.getDescription().isBlank()) errors.add("Description is required");
        if (problem.getDifficulty() == null) errors.add("Difficulty is required");
        if (problem.getProblemConstraints() == null || problem.getProblemConstraints().isBlank()) errors.add("Constraints are required");
        if (problem.getInputFormat() == null || problem.getInputFormat().isBlank()) errors.add("Input format is required");
        if (problem.getOutputFormat() == null || problem.getOutputFormat().isBlank()) errors.add("Output format is required");
        if (problem.getTimeLimitMs() == null || problem.getTimeLimitMs() < properties.getLimits().getMinTimeMs()) errors.add("Time limit too low");
        if (problem.getMemoryLimitKb() == null || problem.getMemoryLimitKb() < properties.getLimits().getMinMemoryKb()) errors.add("Memory limit too low");
        if (problem.getComparator() == null || problem.getComparator().isBlank()) errors.add("Comparator is required");

        // At least one topic
        if (problem.getTopics().isEmpty()) errors.add("At least one topic is required");

        // At least one company
        if (problem.getProblemCompanies().isEmpty()) errors.add("At least one company is required");

        // Test cases
        long sampleCount = problem.getTestCases().stream().filter(tc -> !tc.isHidden()).count();
        long hiddenCount = problem.getTestCases().stream().filter(TestCase::isHidden).count();
        if (sampleCount < properties.getPublish().getMinSampleCases()) {
            errors.add("At least " + properties.getPublish().getMinSampleCases() + " sample test case(s) required");
        }
        if (hiddenCount < properties.getPublish().getMinHiddenCases()) {
            errors.add("At least " + properties.getPublish().getMinHiddenCases() + " hidden test case(s) required");
        }

        // Languages
        if (problem.getProblemLanguages().isEmpty()) {
            warnings.add("No languages configured - question will not be runnable");
        } else if (problem.getProblemLanguages().stream().noneMatch(ProblemLanguage::getEnabled)) {
            warnings.add("No enabled languages - question will not be runnable");
        }

        // Sequence order gaps
        List<Integer> seqOrders = problem.getTestCases().stream()
                .map(TestCase::getSequenceOrder)
                .sorted()
                .toList();
        for (int i = 0; i < seqOrders.size(); i++) {
            if (!seqOrders.get(i).equals(i)) {
                warnings.add("Test case sequence order has gaps");
                break;
            }
        }

        return new PublishCheckResponse(errors.isEmpty(), errors, warnings);
    }

    public StatsResponse getStats() {
        long total = problemRepository.count();
        long draft = problemRepository.countByStatus(ProblemStatus.DRAFT);
        long published = problemRepository.countByStatus(ProblemStatus.PUBLISHED);
        long archived = problemRepository.countByStatus(ProblemStatus.ARCHIVED);

        return new StatsResponse(total, draft, published, archived);
    }

    private void validatePublishReadiness(Problem problem) {
        List<String> errors = new ArrayList<>();

        long sampleCount = problem.getTestCases().stream().filter(tc -> !tc.isHidden()).count();
        long hiddenCount = problem.getTestCases().stream().filter(TestCase::isHidden).count();

        if (sampleCount < properties.getPublish().getMinSampleCases()) {
            errors.add("At least " + properties.getPublish().getMinSampleCases() + " sample test case(s) required");
        }
        if (hiddenCount < properties.getPublish().getMinHiddenCases()) {
            errors.add("At least " + properties.getPublish().getMinHiddenCases() + " hidden test case(s) required");
        }
        if (problem.getTopics().isEmpty()) {
            errors.add("At least one topic is required");
        }
        if (problem.getProblemCompanies().isEmpty()) {
            errors.add("At least one company is required");
        }
        if (problem.getProblemLanguages().isEmpty() ||
                problem.getProblemLanguages().stream().noneMatch(ProblemLanguage::getEnabled)) {
            errors.add("At least one enabled language required");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("PUBLISH_CHECK_FAILED", "Publish validation failed", errors);
        }
    }

    private AdminQuestionResponse mapToAdminResponse(Problem problem) {
        List<AdminQuestionResponse.CompanySummary> companies = problem.getProblemCompanies().stream()
                .map(pc -> new AdminQuestionResponse.CompanySummary(
                        pc.getCompany().getId(),
                        pc.getCompany().getName(),
                        pc.getFrequency()
                ))
                .toList();

        List<AdminQuestionResponse.TestCaseSummary> testCases = problem.getTestCases().stream()
                .map(tc -> new AdminQuestionResponse.TestCaseSummary(
                        tc.getId(),
                        tc.getInput(),
                        tc.getExpectedOutput(),
                        tc.isHidden(),
                        tc.getSequenceOrder(),
                        tc.getExplanation()
                ))
                .toList();

        List<AdminQuestionResponse.LanguageSummary> languages = problem.getProblemLanguages().stream()
                .map(pl -> new AdminQuestionResponse.LanguageSummary(
                        pl.getId().getLanguageId(),
                        pl.getLanguage().getId(),
                        pl.getLanguage().getName(),
                        pl.getLanguage().getFileExtension(),
                        pl.getStarterCode(),
                        pl.getEnabled()
                ))
                .toList();

        List<AdminQuestionResponse.HintSummary> hints = problem.getHints().stream()
                .map(h -> new AdminQuestionResponse.HintSummary(
                        h.getId(),
                        h.getHintOrder(),
                        h.getContent()
                ))
                .toList();

        List<String> topicNames = problem.getTopics().stream()
                .map(Topic::getName)
                .toList();

        return new AdminQuestionResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getStatus(),
                problem.getProblemConstraints(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitKb(),
                problem.getComparator(),
                topicNames,
                companies,
                testCases,
                languages,
                hints,
                problem.getContentRevision(),
                problem.getRowVersion(),
                problem.getCreatedAt(),
                problem.getUpdatedAt(),
                problem.getPublishedAt(),
                problem.getCreatedBy(),
                problem.getUpdatedBy()
        );
    }

    public record PublishCheckResponse(
            boolean canPublish,
            List<String> errors,
            List<String> warnings
    ) {}

    public record StatsResponse(
            long total,
            long draft,
            long published,
            long archived
    ) {}
}