package com.project.backend.question.web;

import com.project.backend.question.dto.response.CompanySummary;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.dto.response.TopicSummary;
import com.project.backend.question.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final QuestionService questionService;

    public CatalogController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // GET /api/v1/topics
    @GetMapping("/topics")
    public ResponseEntity<List<TopicSummary>> getAllTopics() {
        return ResponseEntity.ok(questionService.getAllTopics());
    }

    // GET /api/v1/companies
    @GetMapping("/companies")
    public ResponseEntity<List<CompanySummary>> getAllCompanies() {
        return ResponseEntity.ok(questionService.getAllCompanies());
    }

    // GET /api/v1/languages
    @GetMapping("/languages")
    public ResponseEntity<List<LanguageSummary>> getAllLanguages() {
        return ResponseEntity.ok(questionService.getAllLanguages());
    }
}