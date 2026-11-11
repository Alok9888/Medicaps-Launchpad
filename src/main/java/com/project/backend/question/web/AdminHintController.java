package com.project.backend.question.web;

import com.project.backend.question.dto.request.HintRequest;
import com.project.backend.question.dto.response.HintResponse;
import com.project.backend.question.service.AdminHintService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questions/{questionId}/hints")
@SecurityRequirement(name = "admin-key")
public class AdminHintController {

    private final AdminHintService adminHintService;

    public AdminHintController(AdminHintService adminHintService) {
        this.adminHintService = adminHintService;
    }

    @GetMapping
    public ResponseEntity<List<HintResponse>> getHints(@PathVariable Long questionId) {
        return ResponseEntity.ok(adminHintService.getHints(questionId));
    }

    @PostMapping
    public ResponseEntity<HintResponse> addHint(
            @PathVariable Long questionId,
            @Valid @RequestBody HintRequest request
    ) {
        return ResponseEntity.ok(adminHintService.addHint(questionId, request));
    }

    @PutMapping("/{hintId}")
    public ResponseEntity<HintResponse> updateHint(
            @PathVariable Long questionId,
            @PathVariable Long hintId,
            @Valid @RequestBody HintRequest request
    ) {
        return ResponseEntity.ok(adminHintService.updateHint(questionId, hintId, request));
    }

    @DeleteMapping("/{hintId}")
    public ResponseEntity<Void> deleteHint(
            @PathVariable Long questionId,
            @PathVariable Long hintId
    ) {
        adminHintService.deleteHint(questionId, hintId);
        return ResponseEntity.noContent().build();
    }
}
