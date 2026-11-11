package com.project.backend.question.web;

import com.project.backend.common.api.PageResponse;
import com.project.backend.question.dto.request.QuestionCreateRequest;
import com.project.backend.question.dto.request.QuestionUpdateRequest;
import com.project.backend.question.dto.response.AdminQuestionResponse;
import com.project.backend.question.service.AdminQuestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questions")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminQuestionResponse>> getAllQuestions(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.of(adminQuestionService.getAdminQuestions(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminQuestionResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.getAdminQuestion(id));
    }

    @PostMapping
    public ResponseEntity<AdminQuestionResponse> createQuestion(
            @Valid @RequestBody QuestionCreateRequest request,
            @RequestHeader("X-Admin-User-Id") Long createdBy
    ) {
        return ResponseEntity.ok(adminQuestionService.createQuestion(request, createdBy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminQuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateRequest request,
            @RequestHeader("X-Admin-User-Id") Long updatedBy
    ) {
        return ResponseEntity.ok(adminQuestionService.updateQuestion(id, request, updatedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        adminQuestionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<AdminQuestionResponse> publishQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.publishQuestion(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AdminQuestionResponse> archiveQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.archiveQuestion(id));
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<AdminQuestionResponse> unarchiveQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.unarchiveQuestion(id));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<AdminQuestionResponse> cloneQuestion(
            @PathVariable Long id,
            @RequestHeader("X-Admin-User-Id") Long createdBy
    ) {
        return ResponseEntity.ok(adminQuestionService.cloneQuestion(id, createdBy));
    }

    @GetMapping("/{id}/publish-check")
    public ResponseEntity<AdminQuestionService.PublishCheckResponse> getPublishCheck(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.getPublishCheck(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminQuestionService.StatsResponse> getStats() {
        return ResponseEntity.ok(adminQuestionService.getStats());
    }
}