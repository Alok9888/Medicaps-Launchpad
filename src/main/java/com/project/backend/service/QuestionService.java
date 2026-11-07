
package com.project.backend.service;

import com.project.backend.dto.response.QuestionResponse;
import com.project.backend.dto.response.QuestionSummaryResponse;
import com.project.backend.dto.response.SampleTestCaseResponse;
import com.project.backend.model.Difficulty;
import com.project.backend.model.Problem;
import com.project.backend.model.ProblemStatus;
import com.project.backend.model.Topic;
import com.project.backend.repository.ProblemRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    private final ProblemRepository problemRepository;

    public QuestionService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> getQuestions(
            Difficulty difficulty,
            String topic,
            String search,
            Pageable pageable
    ) {
        Specification<Problem> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only published questions
            predicates.add(
                    cb.equal(root.get("status"), ProblemStatus.PUBLISHED)
            );

            // Difficulty filter
            if (difficulty != null) {
                predicates.add(
                        cb.equal(root.get("difficulty"), difficulty)
                );
            }

            // Topic filter
            if (topic != null && !topic.isBlank()) {
                Join<Problem, Topic> topicJoin =
                        root.join("topics", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                cb.lower(topicJoin.get("name")),
                                topic.trim().toLowerCase()
                        )
                );

                query.distinct(true);
            }

            // Title search
            if (search != null && !search.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + search.trim().toLowerCase() + "%"
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return problemRepository.findAll(specification, pageable)
                .map(problem -> new QuestionSummaryResponse(
                        problem.getId(),
                        problem.getTitle(),
                        problem.getSlug(),
                        problem.getDifficulty()
                ));
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {

        Problem problem = problemRepository
                .findByIdAndStatus(id, ProblemStatus.PUBLISHED)
                .orElseThrow(() ->
                        new RuntimeException("Question not found"));

        return mapToResponse(problem);
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionBySlug(String slug) {

        Problem problem = problemRepository
                .findBySlugAndStatus(slug, ProblemStatus.PUBLISHED)
                .orElseThrow(() ->
                        new RuntimeException("Question not found"));

        return mapToResponse(problem);
    }

    private QuestionResponse mapToResponse(Problem problem) {

        List<String> topics = problem.getTopics()
                .stream()
                .map(Topic::getName)
                .toList();

        List<SampleTestCaseResponse> sampleTestCases =
                problem.getTestCases()
                        .stream()
                        .filter(testCase -> !testCase.isHidden())
                        .map(testCase -> new SampleTestCaseResponse(
                                testCase.getInput(),
                                testCase.getExpectedOutput()
                        ))
                        .toList();

        return new QuestionResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getSlug(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getProblemConstraints(),
                topics,
                sampleTestCases
        );
    }
}
