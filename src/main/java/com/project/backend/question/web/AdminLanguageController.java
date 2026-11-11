package com.project.backend.question.web;

import com.project.backend.question.dto.request.LanguageRequest;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.service.AdminLanguageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/questions/{questionId}/languages")
@SecurityRequirement(name = "admin-key")
public class AdminLanguageController {

    private final AdminLanguageService adminLanguageService;

    public AdminLanguageController(AdminLanguageService adminLanguageService) {
        this.adminLanguageService = adminLanguageService;
    }

    @GetMapping
    public ResponseEntity<List<LanguageSummary>> getLanguages(@PathVariable Long questionId) {
        return ResponseEntity.ok(adminLanguageService.getLanguages(questionId));
    }

    @PostMapping
    public ResponseEntity<LanguageSummary> addLanguage(
            @PathVariable Long questionId,
            @Valid @RequestBody LanguageRequest request
    ) {
        return ResponseEntity.ok(adminLanguageService.addLanguage(questionId, request));
    }

    @PutMapping("/{languageId}")
    public ResponseEntity<LanguageSummary> updateLanguage(
            @PathVariable Long questionId,
            @PathVariable Long languageId,
            @Valid @RequestBody LanguageRequest request
    ) {
        return ResponseEntity.ok(adminLanguageService.updateLanguage(questionId, languageId, request));
    }

    @DeleteMapping("/{languageId}")
    public ResponseEntity<Void> deleteLanguage(
            @PathVariable Long questionId,
            @PathVariable Long languageId
    ) {
        adminLanguageService.deleteLanguage(questionId, languageId);
        return ResponseEntity.noContent().build();
    }
}
