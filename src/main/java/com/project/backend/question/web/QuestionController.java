package com.project.backend.question.web;

import com.project.backend.common.api.PageResponse;
import com.project.backend.question.dto.response.CompanySummary;
import com.project.backend.question.dto.response.HintResponse;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.dto.response.QuestionResponse;
import com.project.backend.question.dto.response.QuestionSummaryResponse;
import com.project.backend.question.dto.response.TopicSummary;
import com.project.backend.question.domain.Difficulty;
import com.project.backend.question.service.QuestionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // GET /api/v1/questions
    @GetMapping
    public ResponseEntity<PageResponse<QuestionSummaryResponse>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) List<String> topic,
            @RequestParam(required = false) List<Long> company,
            @RequestParam(required = false) Long language,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "newest") String sort
    ) {
        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 100)
        );

        Page<QuestionSummaryResponse> result = questionService.getQuestions(
                difficulty,
                topic,
                company,
                language,
                search,
                sort,
                pageable
        );

        return ResponseEntity.ok(PageResponse.of(result));
    }

    // GET /api/v1/questions/random
    @GetMapping("/random")
    public ResponseEntity<QuestionSummaryResponse> getRandomQuestion(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String topic
    ) {
        QuestionSummaryResponse result = questionService.getRandomQuestion(difficulty, topic);
        return ResponseEntity.ok(result);
    }

    // GET /api/v1/questions/{id}/hints/{order}
    @GetMapping("/{id}/hints/{order}")
    public ResponseEntity<HintResponse> getHint(
            @PathVariable Long id,
            @PathVariable Integer order
    ) {
        return ResponseEntity.ok(questionService.getHint(id, order));
    }
}