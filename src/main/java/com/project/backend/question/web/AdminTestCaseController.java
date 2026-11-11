package com.project.backend.question.web;

import com.project.backend.question.dto.request.TestCaseBulkRequest;
import com.project.backend.question.dto.request.TestCaseCreateRequest;
import com.project.backend.question.dto.request.TestCaseReorderRequest;
import com.project.backend.question.dto.request.TestCaseUpdateRequest;
import com.project.backend.question.dto.response.TestCaseSummary;
import com.project.backend.question.service.AdminTestCaseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questions/{questionId}/test-cases")
public class AdminTestCaseController {

    private final AdminTestCaseService adminTestCaseService;

    public AdminTestCaseController(AdminTestCaseService adminTestCaseService) {
        this.adminTestCaseService = adminTestCaseService;
    }

    @GetMapping
    public ResponseEntity<List<TestCaseSummary>> getTestCases(@PathVariable Long questionId) {
        return ResponseEntity.ok(adminTestCaseService.getTestCases(questionId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<TestCaseSummary>> addBulkTestCases(
            @PathVariable Long questionId,
            @Valid @RequestBody TestCaseBulkRequest request
    ) {
        return ResponseEntity.ok(adminTestCaseService.addBulkTestCases(questionId, request));
    }

    @PutMapping("/{testCaseId}")
    public ResponseEntity<TestCaseSummary> updateTestCase(
            @PathVariable Long questionId,
            @PathVariable Long testCaseId,
            @Valid @RequestBody TestCaseUpdateRequest request
    ) {
        return ResponseEntity.ok(adminTestCaseService.updateTestCase(questionId, testCaseId, request));
    }

    @DeleteMapping("/{testCaseId}")
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable Long questionId,
            @PathVariable Long testCaseId
    ) {
        adminTestCaseService.deleteTestCase(questionId, testCaseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<List<TestCaseSummary>> reorderTestCases(
            @PathVariable Long questionId,
            @Valid @RequestBody TestCaseReorderRequest request
    ) {
        return ResponseEntity.ok(adminTestCaseService.reorderTestCases(questionId, request));
    }
}