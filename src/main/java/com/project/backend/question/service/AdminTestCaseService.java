package com.project.backend.question.service;

import com.project.backend.common.exception.NotFoundException;
import com.project.backend.common.exception.ValidationException;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.domain.TestCase;
import com.project.backend.question.dto.request.TestCaseBulkRequest;
import com.project.backend.question.dto.request.TestCaseCreateRequest;
import com.project.backend.question.dto.request.TestCaseReorderRequest;
import com.project.backend.question.dto.request.TestCaseUpdateRequest;
import com.project.backend.question.dto.response.TestCaseSummary;
import com.project.backend.question.repository.ProblemRepository;
import com.project.backend.question.repository.TestCaseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminTestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public AdminTestCaseService(TestCaseRepository testCaseRepository, ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional
    public List<TestCaseSummary> addBulkTestCases(Long problemId, TestCaseBulkRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify test cases of a published question");
        }

        List<TestCaseSummary> result = new ArrayList<>();
        for (int i = 0; i < request.testCases().size(); i++) {
            TestCaseCreateRequest tc = request.testCases().get(i);
            TestCase testCase = new TestCase(
                    tc.input(),
                    tc.expectedOutput(),
                    tc.hidden(),
                    tc.sequenceOrder() != null ? tc.sequenceOrder() : i
            );
            testCase.setExplanation(tc.explanation());
            problem.addTestCase(testCase);
            TestCase saved = testCaseRepository.save(testCase);
            result.add(mapToSummary(saved));
        }
        return result;
    }

    @Transactional
    public TestCaseSummary updateTestCase(Long problemId, Long testCaseId, TestCaseUpdateRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify test cases of a published question");
        }

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new NotFoundException("TEST_CASE_NOT_FOUND", "Test case not found"));

        if (!testCase.getProblem().getId().equals(problemId)) {
            throw new ValidationException("TEST_CASE_MISMATCH", "Test case does not belong to this question");
        }

        testCase.setInput(request.input());
        testCase.setExpectedOutput(request.expectedOutput());
        testCase.setHidden(request.hidden());
        testCase.setSequenceOrder(request.sequenceOrder());
        testCase.setExplanation(request.explanation());

        TestCase saved = testCaseRepository.save(testCase);
        return mapToSummary(saved);
    }

    @Transactional
    public void deleteTestCase(Long problemId, Long testCaseId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify test cases of a published question");
        }

        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new NotFoundException("TEST_CASE_NOT_FOUND", "Test case not found"));

        if (!testCase.getProblem().getId().equals(problemId)) {
            throw new ValidationException("TEST_CASE_MISMATCH", "Test case does not belong to this question");
        }

        problem.removeTestCase(testCase);
        testCaseRepository.delete(testCase);
    }

    @Transactional
    public List<TestCaseSummary> reorderTestCases(Long problemId, TestCaseReorderRequest request) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        if (problem.getStatus() == ProblemStatus.PUBLISHED) {
            throw new ValidationException("ILLEGAL_STATE", "Cannot modify test cases of a published question");
        }

        List<TestCaseSummary> result = new ArrayList<>();
        for (TestCaseReorderRequest.ReorderItem item : request.items()) {
            TestCase testCase = testCaseRepository.findById(item.id())
                    .orElseThrow(() -> new NotFoundException("TEST_CASE_NOT_FOUND", "Test case not found: " + item.id()));

            if (!testCase.getProblem().getId().equals(problemId)) {
                throw new ValidationException("TEST_CASE_MISMATCH", "Test case does not belong to this question");
            }

            testCase.setSequenceOrder(item.sequenceOrder());
            TestCase saved = testCaseRepository.save(testCase);
            result.add(mapToSummary(saved));
        }
        return result;
    }

    public List<TestCaseSummary> getTestCases(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));

        return testCaseRepository.findByProblemIdOrderBySequenceOrderAsc(problemId).stream()
                .map(this::mapToSummary)
                .toList();
    }

    private TestCaseSummary mapToSummary(TestCase testCase) {
        return new TestCaseSummary(
                testCase.getId(),
                testCase.getInput(),
                testCase.getExpectedOutput(),
                testCase.isHidden(),
                testCase.getSequenceOrder(),
                testCase.getExplanation()
        );
    }
}