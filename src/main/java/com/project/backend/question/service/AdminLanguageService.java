package com.project.backend.question.service;

import com.project.backend.common.exception.NotFoundException;
import com.project.backend.common.exception.ValidationException;
import com.project.backend.question.domain.Language;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemLanguage;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.dto.request.LanguageRequest;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.repository.LanguageRepository;
import com.project.backend.question.repository.ProblemLanguageRepository;
import com.project.backend.question.repository.ProblemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminLanguageService {

    private final ProblemLanguageRepository problemLanguageRepository;
    private final ProblemRepository problemRepository;
    private final LanguageRepository languageRepository;

    public AdminLanguageService(
            ProblemLanguageRepository problemLanguageRepository,
            ProblemRepository problemRepository,
            LanguageRepository languageRepository
    ) {
        this.problemLanguageRepository = problemLanguageRepository;
        this.problemRepository = problemRepository;
        this.languageRepository = languageRepository;
    }

    @Transactional
    public LanguageSummary addLanguage(Long problemId, LanguageRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify languages of a published question");
        }

        Language language = languageRepository.findById(request.languageId())
                .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not found: " + request.languageId()));

        if (problemLanguageRepository.findByProblemIdAndLanguageId(problemId, request.languageId()).isPresent()) {
            throw new ValidationException("DUPLICATE_LANGUAGE", "Language already configured for this question");
        }

        ProblemLanguage pl = new ProblemLanguage(problem, language, request.starterCode(), request.enabled());
        problem.addProblemLanguage(pl);
        ProblemLanguage saved = problemLanguageRepository.save(pl);
        return mapToSummary(saved);
    }

    @Transactional
    public LanguageSummary updateLanguage(Long problemId, Long languageId, LanguageRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify languages of a published question");
        }

        ProblemLanguage pl = problemLanguageRepository.findByProblemIdAndLanguageId(problemId, languageId)
                .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not configured for this question"));

        pl.setStarterCode(request.starterCode());
        pl.setEnabled(request.enabled());

        ProblemLanguage saved = problemLanguageRepository.save(pl);
        return mapToSummary(saved);
    }

    @Transactional
    public void deleteLanguage(Long problemId, Long languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify languages of a published question");
        }

        ProblemLanguage pl = problemLanguageRepository.findByProblemIdAndLanguageId(problemId, languageId)
                .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not configured for this question"));

        problem.removeProblemLanguage(pl);
        problemLanguageRepository.delete(pl);
    }

    public List<LanguageSummary> getLanguages(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        return problemLanguageRepository.findByProblemId(problemId).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    private LanguageSummary mapToSummary(ProblemLanguage pl) {
        return new LanguageSummary(
                pl.getLanguage().getId(),
                pl.getLanguage().getName(),
                pl.getLanguage().getFileExtension(),
                pl.getStarterCode(),
                pl.getEnabled()
        );
    }
}