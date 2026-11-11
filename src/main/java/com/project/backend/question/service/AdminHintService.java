package com.project.backend.question.service;

import com.project.backend.common.exception.NotFoundException;
import com.project.backend.common.exception.ValidationException;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemHint;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.dto.request.HintRequest;
import com.project.backend.question.dto.response.HintResponse;
import com.project.backend.question.repository.ProblemHintRepository;
import com.project.backend.question.repository.ProblemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminHintService {

    private final ProblemHintRepository problemHintRepository;
    private final ProblemRepository problemRepository;

    public AdminHintService(ProblemHintRepository problemHintRepository, ProblemRepository problemRepository) {
        this.problemHintRepository = problemHintRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional
    public HintResponse addHint(Long problemId, HintRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify hints of a published question");
        }

        // Check if hint order already exists
        if (problemHintRepository.findByProblemIdAndHintOrder(problemId, request.order()).isPresent()) {
            throw new ValidationException("DUPLICATE_HINT_ORDER", "Hint with this order already exists");
        }

        ProblemHint hint = new ProblemHint(problem, request.order(), request.content());
        problem.addHint(hint);
        ProblemHint saved = problemHintRepository.save(hint);
        return mapToResponse(saved);
    }

    @Transactional
    public HintResponse updateHint(Long problemId, Long hintId, HintRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify hints of a published question");
        }

        ProblemHint hint = problemHintRepository.findById(hintId)
                .orElseThrow(() -> new NotFoundException("HINT_NOT_FOUND", "Hint not found"));

        if (!hint.getProblem().getId().equals(problemId)) {
            throw new ValidationException("HINT_MISMATCH", "Hint does not belong to this question");
        }

        // Check if new order conflicts with another hint
        problemHintRepository.findByProblemIdAndHintOrder(problemId, request.order())
                .filter(h -> !h.getId().equals(hintId))
                .ifPresent(h -> {
                    throw new ValidationException("DUPLICATE_HINT_ORDER", "Hint with this order already exists");
                });

        hint.setHintOrder(request.order());
        hint.setContent(request.content());

        ProblemHint saved = problemHintRepository.save(hint);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteHint(Long problemId, Long hintId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify hints of a published question");
        }

        ProblemHint hint = problemHintRepository.findById(hintId)
                .orElseThrow(() -> new NotFoundException("HINT_NOT_FOUND", "Hint not found"));

        if (!hint.getProblem().getId().equals(problemId)) {
            throw new ValidationException("HINT_MISMATCH", "Hint does not belong to this question");
        }

        problem.removeHint(hint);
        problemHintRepository.delete(hint);
    }

    public List<HintResponse> getHints(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        return problemHintRepository.findByProblemIdOrderByHintOrderAsc(problemId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private HintResponse mapToResponse(ProblemHint hint) {
        return new HintResponse(
                hint.getHintOrder(),
                hint.getContent(),
                hint.getProblem().getHints().size()
        );
    }
}