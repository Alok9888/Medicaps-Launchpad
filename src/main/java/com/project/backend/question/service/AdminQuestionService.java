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
    private final PublishCheckService publishCheckService;
    private final SlugService slugService;
    private final SanitizationService sanitizationService;
    private final EditRulesService editRulesService;

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
            EntityManager entityManager,
            PublishCheckService publishCheckService,
            SlugService slugService,
            SanitizationService sanitizationService,
            EditRulesService editRulesService
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
        this.publishCheckService = publishCheckService;
        this.slugService = slugService;
        this.sanitizationService = sanitizationService;
        this.editRulesService = editRulesService;
    }

    @Transactional
    public AdminQuestionResponse createQuestion(QuestionCreateRequest request, Long createdBy) {
        // Generate or sanitize slug
        String slug = request.slug() != null && !request.slug().isBlank()
                ? slugService.sanitizeSlug(request.slug())
                : slugService.generateSlug(request.title());

        if (!slugService.isValidSlug(slug)) {
            throw new ValidationException("INVALID_SLUG", "Slug must be lowercase alphanumeric with hyphens only");
        }

        // Ensure slug uniqueness
        slug = slugService.makeUnique(slug);

        // Sanitize text fields
        String sanitizedTitle = sanitizationService.sanitizePlainText(request.title());
        String sanitizedDescription = sanitizationService.sanitizeHtml(request.description());
        String sanitizedConstraints = sanitizationService.sanitizeHtml(request.constraints());
        String sanitizedInputFormat = sanitizationService.sanitizeHtml(request.inputFormat());
        String sanitizedOutputFormat = sanitizationService.sanitizeHtml(request.outputFormat());

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
                sanitizedTitle,
                slug,
                sanitizedDescription,
                request.difficulty(),
                sanitizedConstraints
        );
        problem.setInputFormat(sanitizedInputFormat);
        problem.setOutputFormat(sanitizedOutputFormat);
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

        // Validate edit rules based on status
        Set<String> changedFields = detectChangedFields(problem, request);
        editRulesService.validateEditAllowed(problem, changedFields);

        // Optimistic locking check
        if (!problem.getRowVersion().equals(request.rowVersion())) {
            throw new ConflictException("STALE_VERSION", "Question was modified by another user");
        }

        // Validate slug uniqueness (excluding current)
        if (!problem.getSlug().equals(request.slug()) && problemRepository.existsBySlug(request.slug())) {
            throw new ConflictException("DUPLICATE_SLUG", "A question with this slug already exists");
        }

        // Sanitize text fields
        String sanitizedTitle = sanitizationService.sanitizePlainText(request.title());
        String sanitizedDescription = sanitizationService.sanitizeHtml(request.description());
        String sanitizedConstraints = sanitizationService.sanitizeHtml(request.constraints());
        String sanitizedInputFormat = sanitizationService.sanitizeHtml(request.inputFormat());
        String sanitizedOutputFormat = sanitizationService.sanitizeHtml(request.outputFormat());

        // Update basic fields
        problem.setTitle(sanitizedTitle);
        problem.setSlug(request.slug()); // slug is immutable per edit rules, but validate anyway
        problem.setDescription(sanitizedDescription);
        problem.setDifficulty(request.difficulty());
        problem.setProblemConstraints(sanitizedConstraints);
        problem.setInputFormat(sanitizedInputFormat);
        problem.setOutputFormat(sanitizedOutputFormat);
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

        editRulesService.validateCanDelete(problem);

        problemRepository.delete(problem);
    }

    @Transactional
    public AdminQuestionResponse publishQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        editRulesService.validateCanPublish(problem);

        // Run comprehensive publish check
        PublishCheckService.PublishCheckResult checkResult = publishCheckService.check(problem);
        if (!checkResult.canPublish()) {
            throw new ValidationException("PUBLISH_CHECK_FAILED", "Publish validation failed", checkResult.errors());
        }

        problem.publish();
        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse archiveQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        editRulesService.validateCanArchive(problem);

        problem.archive();
        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse unarchiveQuestion(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        editRulesService.validateCanUnarchive(problem);

        problem.unarchive();
        Problem saved = problemRepository.save(problem);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public AdminQuestionResponse cloneQuestion(Long id, Long createdBy) {
        Problem original = problemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        editRulesService.validateCanClone(original);

        // Generate unique slug for clone
        String cloneSlug = slugService.makeUnique(original.getSlug() + "-copy");

        Problem clone = new Problem(
                original.getTitle() + " (Copy)",
                cloneSlug,
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
                    sanitizationService.sanitizeCode(tc.getInput()),
                    sanitizationService.sanitizeCode(tc.getExpectedOutput()),
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

        PublishCheckService.PublishCheckResult result = publishCheckService.check(problem);

        return new PublishCheckResponse(
                result.canPublish(),
                result.errors(),
                result.warnings()
        );
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

    private Set<String> detectChangedFields(Problem problem, QuestionUpdateRequest request) {
        Set<String> changed = new HashSet<>();

        if (!java.util.Objects.equals(problem.getTitle(), request.title())) changed.add("title");
        if (!java.util.Objects.equals(problem.getSlug(), request.slug())) changed.add("slug");
        if (!java.util.Objects.equals(problem.getDescription(), request.description())) changed.add("description");
        if (problem.getDifficulty() != request.difficulty()) changed.add("difficulty");
        if (!java.util.Objects.equals(problem.getProblemConstraints(), request.constraints())) changed.add("problemConstraints");
        if (!java.util.Objects.equals(problem.getInputFormat(), request.inputFormat())) changed.add("inputFormat");
        if (!java.util.Objects.equals(problem.getOutputFormat(), request.outputFormat())) changed.add("outputFormat");
        if (!java.util.Objects.equals(problem.getTimeLimitMs(), request.timeLimitMs())) changed.add("timeLimitMs");
        if (!java.util.Objects.equals(problem.getMemoryLimitKb(), request.memoryLimitKb())) changed.add("memoryLimitKb");
        if (!java.util.Objects.equals(problem.getComparator(), request.comparator())) changed.add("comparator");
        if (!java.util.Objects.equals(problem.getFloatEpsilon(), request.floatEpsilon())) changed.add("floatEpsilon");

        // Check topics
        Set<String> currentTopics = new HashSet<>();
        for (Topic t : problem.getTopics()) {
            currentTopics.add(t.getName());
        }
        Set<String> newTopics = new HashSet<>(request.topics());
        if (!currentTopics.equals(newTopics)) changed.add("topics");

        // Check companies
        Set<Long> currentCompanyIds = new HashSet<>();
        for (ProblemCompany pc : problem.getProblemCompanies()) {
            currentCompanyIds.add(pc.getCompany().getId());
        }
        Set<Long> newCompanyIds = new HashSet<>();
        for (QuestionUpdateRequest.CompanyRequest cr : request.companies()) {
            newCompanyIds.add(cr.id());
        }
        if (!currentCompanyIds.equals(newCompanyIds)) changed.add("companies");

        // Check test cases, languages, hints - simplified comparison
        if (request.testCases() != null) changed.add("testCases");
        if (request.languages() != null) changed.add("languages");
        if (request.hints() != null) changed.add("hints");

        return changed;
    }
}