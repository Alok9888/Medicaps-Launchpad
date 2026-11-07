
package com.project.backend.controller;

import com.project.backend.dto.response.QuestionResponse;
import com.project.backend.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.backend.dto.response.QuestionSummaryResponse;
import com.project.backend.model.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // GET /api/v1/questions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                questionService.getQuestionById(id)
        );
    }

    // GET /api/v1/questions/slug/{slug}
    @GetMapping("/slug/{slug}")
    public ResponseEntity<QuestionResponse> getQuestionBySlug(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(
                questionService.getQuestionBySlug(slug)
        );
    }
 

    @GetMapping
    public ResponseEntity<Page<QuestionSummaryResponse>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by("createdAt").descending()
        );

        return ResponseEntity.ok(
                questionService.getQuestions(
                        difficulty,
                        topic,
                        search,
                        pageable
                )
        );
    }

}
