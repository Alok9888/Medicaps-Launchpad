package com.project.backend.question.internal;

import com.project.backend.common.exception.NotFoundException;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.repository.ProblemRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class QuestionLookupApi {

    private final ProblemRepository problemRepository;

    public QuestionLookupApi(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Cacheable(value = "questionById", key = "#id", unless = "#result == null")
    public Optional<Problem> findById(Long id) {
        return problemRepository.findById(id);
    }

    @Cacheable(value = "questionBySlug", key = "#slug", unless = "#result == null")
    public Optional<Problem> findBySlug(String slug) {
        return problemRepository.findBySlug(slug);
    }

    @Cacheable(value = "publishedQuestionById", key = "#id", unless = "#result == null")
    public Optional<Problem> findPublishedById(Long id) {
        return problemRepository.findByIdAndStatus(id, ProblemStatus.PUBLISHED);
    }

    @Cacheable(value = "publishedQuestionBySlug", key = "#slug", unless = "#result == null")
    public Optional<Problem> findPublishedBySlug(String slug) {
        return problemRepository.findBySlugAndStatus(slug, ProblemStatus.PUBLISHED);
    }

    @Cacheable(value = "questionsByIds", key = "#ids", unless = "#result.isEmpty()")
    public List<Problem> findByIds(List<Long> ids) {
        return problemRepository.findAllById(ids);
    }

    @Cacheable(value = "publishedQuestionsByIds", key = "#ids", unless = "#result.isEmpty()")
    public List<Problem> findPublishedByIds(List<Long> ids) {
        return problemRepository.findPublishedByIds(ids);
    }

    public Problem getPublishedByIdOrThrow(Long id) {
        return findPublishedById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
    }

    public Problem getPublishedBySlugOrThrow(String slug) {
        return findPublishedBySlug(slug)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
    }

    public boolean existsById(Long id) {
        return problemRepository.existsById(id);
    }

    public boolean existsBySlug(String slug) {
        return problemRepository.existsBySlug(slug);
    }

    public long countPublished() {
        return problemRepository.countByStatus(ProblemStatus.PUBLISHED);
    }
}