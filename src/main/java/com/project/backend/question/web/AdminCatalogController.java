package com.project.backend.question.web;

import com.project.backend.question.dto.request.CompanyCreateRequest;
import com.project.backend.question.dto.request.LanguageCreateRequest;
import com.project.backend.question.dto.request.TopicCreateRequest;
import com.project.backend.question.dto.response.CompanySummary;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.dto.response.TopicSummary;
import com.project.backend.question.service.AdminCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalog")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    public AdminCatalogController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    // Topics
    @GetMapping("/topics")
    public ResponseEntity<List<TopicSummary>> getAllTopics() {
        return ResponseEntity.ok(adminCatalogService.getAllTopics());
    }

    @PostMapping("/topics")
    public ResponseEntity<TopicSummary> createTopic(@Valid @RequestBody TopicCreateRequest request) {
        return ResponseEntity.ok(adminCatalogService.createTopic(request));
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<TopicSummary> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicCreateRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.updateTopic(id, request));
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        adminCatalogService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    // Companies
    @GetMapping("/companies")
    public ResponseEntity<List<CompanySummary>> getAllCompanies() {
        return ResponseEntity.ok(adminCatalogService.getAllCompanies());
    }

    @PostMapping("/companies")
    public ResponseEntity<CompanySummary> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return ResponseEntity.ok(adminCatalogService.createCompany(request));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<CompanySummary> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyCreateRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.updateCompany(id, request));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        adminCatalogService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    // Languages
    @GetMapping("/languages")
    public ResponseEntity<List<LanguageSummary>> getAllLanguages() {
        return ResponseEntity.ok(adminCatalogService.getAllLanguages());
    }

    @PostMapping("/languages")
    public ResponseEntity<LanguageSummary> createLanguage(@Valid @RequestBody LanguageCreateRequest request) {
        return ResponseEntity.ok(adminCatalogService.createLanguage(request));
    }

    @PutMapping("/languages/{id}")
    public ResponseEntity<LanguageSummary> updateLanguage(
            @PathVariable Long id,
            @Valid @RequestBody LanguageCreateRequest request
    ) {
        return ResponseEntity.ok(adminCatalogService.updateLanguage(id, request));
    }

    @DeleteMapping("/languages/{id}")
    public ResponseEntity<Void> deleteLanguage(@PathVariable Long id) {
        adminCatalogService.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }
}