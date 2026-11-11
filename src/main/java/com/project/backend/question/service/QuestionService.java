package com.project.backend.question.service;

import com.project.backend.common.exception.NotFoundException;
import com.project.backend.question.domain.Difficulty;
import com.project.backend.question.domain.Language;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemCompany;
import com.project.backend.question.domain.ProblemLanguage;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.domain.Topic;
import com.project.backend.question.dto.response.CompanySummary;
import com.project.backend.question.dto.response.HintResponse;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.dto.response.QuestionResponse;
import com.project.backend.question.dto.response.QuestionSummaryResponse;
import com.project.backend.question.dto.response.SampleTestCaseResponse;
import com.project.backend.question.dto.response.TopicSummary;
import com.project.backend.question.repository.LanguageRepository;
import com.project.backend.question.repository.ProblemCompanyRepository;
import com.project.backend.question.repository.ProblemHintRepository;
import com.project.backend.question.repository.ProblemLanguageRepository;
import com.project.backend.question.repository.ProblemRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final ProblemRepository problemRepository;
    private final ProblemLanguageRepository problemLanguageRepository;
    private final ProblemCompanyRepository problemCompanyRepository;
    private final ProblemHintRepository problemHintRepository;
    private final LanguageRepository languageRepository;
    private final Random random = new Random();

    public QuestionService(
            ProblemRepository problemRepository,
            ProblemLanguageRepository problemLanguageRepository,
            ProblemCompanyRepository problemCompanyRepository,
            ProblemHintRepository problemHintRepository,
            LanguageRepository languageRepository
    ) {
        this.problemRepository = problemRepository;
        this.problemLanguageRepository = problemLanguageRepository;
        this.problemCompanyRepository = problemCompanyRepository;
        this.problemHintRepository = problemHintRepository;
        this.languageRepository = languageRepository;
    }

    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> getQuestions(
            Difficulty difficulty,
            List<String> topics,
            List<Long> companies,
            Long languageId,
            String search,
            String sort,
            Pageable pageable
    ) {
        Specification<Problem> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only published questions
            predicates.add(cb.equal(root.get("status"), ProblemStatus.PUBLISHED));

            // Difficulty filter (repeatable)
            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }

            // Topics filter (repeatable)
            if (topics != null && !topics.isEmpty()) {
                Join<Problem, Topic> topicJoin = root.join("topics", JoinType.INNER);
                predicates.add(cb.lower(topicJoin.get("name")).in(
                        topics.stream().map(String::trim).map(String::toLowerCase).toList()
                ));
                query.distinct(true);
            }

            // Companies filter (repeatable)
            if (companies != null && !companies.isEmpty()) {
                Join<Problem, ProblemCompany> pcJoin = root.join("problemCompanies", JoinType.INNER);
                predicates.add(pcJoin.get("company").get("id").in(companies));
                query.distinct(true);
            }

            // Language filter
            if (languageId != null) {
                Join<Problem, ProblemLanguage> plJoin = root.join("problemLanguages", JoinType.INNER);
                predicates.add(cb.and(
                        cb.equal(plJoin.get("language").get("id"), languageId),
                        cb.equal(plJoin.get("enabled"), true)
                ));
                query.distinct(true);
            }

            // Title search
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + search.trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Apply sorting
        Pageable sortedPageable = applySort(pageable, sort);

        Page<Problem> problemPage = problemRepository.findAll(specification, sortedPageable);

        // Fetch associated topics and companies for the page content to avoid N+1
        List<Long> problemIds = problemPage.getContent().stream()
                .map(Problem::getId)
                .toList();

        Map<Long, List<String>> topicsMap = fetchTopicsMap(problemIds);
        Map<Long, List<QuestionSummaryResponse.CompanySummary>> companiesMap = fetchCompaniesMap(problemIds);

        return problemPage.map(problem -> new QuestionSummaryResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDifficulty(),
                topicsMap.getOrDefault(problem.getId(), List.of()),
                companiesMap.getOrDefault(problem.getId(), List.of())
        ));
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        Problem problem = problemRepository
                .findByIdAndStatus(id, ProblemStatus.PUBLISHED)
                .orElseThrow(() ->
                        new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
        return mapToFullResponse(problem);
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionBySlug(String slug) {
        Problem problem = problemRepository
                .findBySlugAndStatus(slug, ProblemStatus.PUBLISHED)
                .orElseThrow(() ->
                        new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
        return mapToFullResponse(problem);
    }

    @Transactional(readOnly = true)
    public QuestionSummaryResponse getRandomQuestion(Difficulty difficulty, String topic) {
        List<QuestionSummaryResponse> questions = getQuestions(
                difficulty,
                topic != null ? List.of(topic) : null,
                null,
                null,
                null,
                "random",
                org.springframework.data.domain.PageRequest.of(0, 50)
        ).getContent();

        if (questions.isEmpty()) {
            throw new NotFoundException("QUESTION_NOT_FOUND", "No questions match the criteria");
        }

        return questions.get(random.nextInt(questions.size()));
    }

    @Transactional(readOnly = true)
    public HintResponse getHint(Long questionId, Integer order) {
        Problem problem = problemRepository
                .findByIdAndStatus(questionId, ProblemStatus.PUBLISHED)
                .orElseThrow(() ->
                        new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        return problemHintRepository.findByProblemIdAndHintOrder(questionId, order)
                .map(hint -> new HintResponse(
                        hint.getHintOrder(),
                        hint.getContent(),
                        problem.getHints().size()
                ))
                .orElseThrow(() ->
                        new NotFoundException("NOT_FOUND", "Hint not found"));
    }

    @Transactional(readOnly = true)
    public List<TopicSummary> getAllTopics() {
        return problemRepository.findPublishedTopicCounts().stream()
                .map(row -> new TopicSummary(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanySummary> getAllCompanies() {
        return problemCompanyRepository.findPublishedCompanyCounts().stream()
                .map(row -> new CompanySummary(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LanguageSummary> getAllLanguages() {
        return languageRepository.findByIsActiveTrue().stream()
                .map(lang -> new LanguageSummary(
                        lang.getId(),
                        lang.getName(),
                        lang.getFileExtension()
                ))
                .toList();
    }

    private Pageable applySort(Pageable pageable, String sort) {
        if (sort == null || sort.isBlank()) {
            sort = "newest";
        }
        Sort.Order order = switch (sort.toLowerCase()) {
            case "oldest" -> Sort.Order.asc("createdAt");
            case "title" -> Sort.Order.asc("title");
            case "difficulty" -> Sort.Order.asc("difficulty");
            case "random" -> null;
            default -> Sort.Order.desc("createdAt"); // newest
        };
        if (order == null) {
            return pageable; // random handled separately
        }
        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(order)
        );
    }

    private Map<Long, List<String>> fetchTopicsMap(List<Long> problemIds) {
        if (problemIds.isEmpty()) return Collections.emptyMap();
        return problemRepository.findTopicsByProblemIds(problemIds).stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (String) row[1], Collectors.toList())
                ));
    }

    private Map<Long, List<QuestionSummaryResponse.CompanySummary>> fetchCompaniesMap(List<Long> problemIds) {
        if (problemIds.isEmpty()) return Collections.emptyMap();
        return problemCompanyRepository.findByProblemIds(problemIds).stream()
                .collect(Collectors.groupingBy(
                        pc -> pc.getProblem().getId(),
                        Collectors.mapping(pc -> new QuestionSummaryResponse.CompanySummary(
                                pc.getCompany().getId(),
                                pc.getCompany().getName(),
                                pc.getFrequency()
                        ), Collectors.toList())
                ));
    }

    private QuestionResponse mapToFullResponse(Problem problem) {
        List<String> topics = problem.getTopics()
                .stream()
                .map(Topic::getName)
                .toList();

        List<QuestionResponse.CompanySummary> companies = problem.getProblemCompanies().stream()
                .map(pc -> new QuestionResponse.CompanySummary(
                        pc.getCompany().getId(),
                        pc.getCompany().getName(),
                        pc.getFrequency()
                ))
                .toList();

        List<SampleTestCaseResponse> sampleTestCases = problem.getTestCases().stream()
                .filter(tc -> !tc.isHidden())
                .map(tc -> new SampleTestCaseResponse(
                        tc.getInput(),
                        tc.getExpectedOutput(),
                        tc.getExplanation()
                ))
                .toList();

        List<QuestionResponse.LanguageSummary> languages = problem.getProblemLanguages().stream()
                .filter(ProblemLanguage::getEnabled)
                .map(pl -> new QuestionResponse.LanguageSummary(
                        pl.getLanguage().getId(),
                        pl.getLanguage().getName(),
                        pl.getLanguage().getFileExtension(),
                        pl.getStarterCode(),
                        pl.getEnabled()
                ))
                .toList();

        Integer hintCount = problem.getHints().size();

        return new QuestionResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getProblemConstraints(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getTimeLimitMs(),
                problem.getMemoryLimitKb(),
                topics,
                companies,
                sampleTestCases,
                languages,
                hintCount
        );
    }
}