package com.project.backend.question.web;

import com.project.backend.common.api.PageResponse;
import com.project.backend.question.dto.request.QuestionCreateRequest;
import com.project.backend.question.dto.request.QuestionUpdateRequest;
import com.project.backend.question.dto.response.AdminQuestionResponse;
import com.project.backend.question.domain.Difficulty;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.service.AdminQuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminQuestionController.class)
@DisplayName("AdminQuestionController Tests")
class AdminQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminQuestionService adminQuestionService;

    private AdminQuestionResponse createAdminQuestionResponse(Long id, String title, String slug, Difficulty difficulty, ProblemStatus status) {
        return new AdminQuestionResponse(
                id, title, slug, "Description", difficulty, status,
                "Constraints", "Input format", "Output format",
                2000, 262144, "TRIM_LINES",
                List.of("Array"),
                List.of(new AdminQuestionResponse.CompanySummary(1L, "Google", 5)),
                List.of(new AdminQuestionResponse.TestCaseSummary(1L, "input", "output", false, 0, "explanation")),
                List.of(new AdminQuestionResponse.LanguageSummary(1L, 1L, "Python", "py", "starter", true)),
                List.of(new AdminQuestionResponse.HintSummary(1L, 1, "hint")),
                1, 0L, java.time.Instant.now(), java.time.Instant.now(), java.time.Instant.now(), 1L, 1L
        );
    }

    private QuestionCreateRequest createQuestionCreateRequest() {
        return new QuestionCreateRequest(
                "Two Sum",
                "two-sum",
                "Given an array...",
                Difficulty.EASY,
                "Constraints",
                "Input format",
                "Output format",
                2000,
                262144,
                "TRIM_LINES",
                null,
                List.of("Array"),
                List.of(new QuestionCreateRequest.CompanyRequest(1L, 5)),
                List.of(new QuestionCreateRequest.TestCaseRequest("[2,7,11,15]", "[0,1]", false, 0, "Explanation")),
                List.of(new QuestionCreateRequest.LanguageRequest(1L, "starter", true)),
                List.of("Hint 1")
        );
    }

    @Nested
    @DisplayName("GET /api/v1/admin/questions - List Questions")
    class GetAllQuestions {

        @Test
        @DisplayName("Should return 200 with paginated questions")
        void getAllQuestions_shouldReturnPagedQuestions() throws Exception {
            Page<AdminQuestionResponse> page = new PageImpl<>(List.of(
                    createAdminQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY, ProblemStatus.DRAFT),
                    createAdminQuestionResponse(2L, "Add Two Numbers", "add-two-numbers", Difficulty.MEDIUM, ProblemStatus.PUBLISHED)
            ));

            when(adminQuestionService.getAdminQuestions(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/admin/questions")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(1)))
                    .andExpect(jsonPath("$.content[1].id", is(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)));
        }

        @Test
        @DisplayName("Should return 401 without X-Admin-Key header")
        void getAllQuestions_withoutAdminKey_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/admin/questions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/questions/{id} - Get Question")
    class GetQuestion {

        @Test
        @DisplayName("Should return 200 with question details")
        void getQuestion_whenExists_shouldReturnQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY, ProblemStatus.DRAFT);
            when(adminQuestionService.getAdminQuestion(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/admin/questions/1")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("Two Sum")))
                    .andExpect(jsonPath("$.status", is("DRAFT")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/questions - Create Question")
    class CreateQuestion {

        @Test
        @DisplayName("Should create question and return 200")
        void createQuestion_shouldReturnCreatedQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY, ProblemStatus.DRAFT);
            when(adminQuestionService.createQuestion(any(QuestionCreateRequest.class), eq(1L)))
                    .thenReturn(response);

            String json = """
                    {
                        "title": "Two Sum",
                        "slug": "two-sum",
                        "description": "Given an array...",
                        "difficulty": "EASY",
                        "constraints": "Constraints",
                        "inputFormat": "Input format",
                        "outputFormat": "Output format",
                        "timeLimitMs": 2000,
                        "memoryLimitKb": 262144,
                        "comparator": "TRIM_LINES",
                        "topics": ["Array"],
                        "companies": [{"id": 1, "frequency": 5}],
                        "testCases": [{"input": "[2,7,11,15]", "expectedOutput": "[0,1]", "hidden": false, "sequenceOrder": 0, "explanation": "Explanation"}],
                        "languages": [{"languageId": 1, "starterCode": "starter", "enabled": true}],
                        "hints": ["Hint 1"]
                    }
                    """;

            mockMvc.perform(post("/api/v1/admin/questions")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .header("X-Admin-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("Two Sum")));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/admin/questions/{id} - Update Question")
    class UpdateQuestion {

        @Test
        @DisplayName("Should update question and return 200")
        void updateQuestion_shouldReturnUpdatedQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(1L, "Two Sum Updated", "two-sum", Difficulty.MEDIUM, ProblemStatus.DRAFT);
            when(adminQuestionService.updateQuestion(eq(1L), any(QuestionUpdateRequest.class), eq(1L)))
                    .thenReturn(response);

            String json = """
                    {
                        "title": "Two Sum Updated",
                        "slug": "two-sum",
                        "description": "Updated description",
                        "difficulty": "MEDIUM",
                        "constraints": "Constraints",
                        "inputFormat": "Input format",
                        "outputFormat": "Output format",
                        "timeLimitMs": 2000,
                        "memoryLimitKb": 262144,
                        "comparator": "TRIM_LINES",
                        "topics": ["Array"],
                        "companies": [{"id": 1, "frequency": 5}],
                        "testCases": [{"id": 1, "input": "[2,7,11,15]", "expectedOutput": "[0,1]", "hidden": false, "sequenceOrder": 0, "explanation": "Explanation"}],
                        "languages": [{"languageId": 1, "starterCode": "starter", "enabled": true}],
                        "hints": ["Hint 1"],
                        "rowVersion": 0
                    }
                    """;

            mockMvc.perform(put("/api/v1/admin/questions/1")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .header("X-Admin-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title", is("Two Sum Updated")))
                    .andExpect(jsonPath("$.difficulty", is("MEDIUM")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/admin/questions/{id} - Delete Question")
    class DeleteQuestion {

        @Test
        @DisplayName("Should delete question and return 204")
        void deleteQuestion_shouldReturnNoContent() throws Exception {
            doNothing().when(adminQuestionService).deleteQuestion(1L);

            mockMvc.perform(delete("/api/v1/admin/questions/1")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production"))
                    .andExpect(status().isNoContent());

            verify(adminQuestionService).deleteQuestion(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/questions/{id}/publish - Publish Question")
    class PublishQuestion {

        @Test
        @DisplayName("Should publish question and return 200")
        void publishQuestion_shouldReturnPublishedQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY, ProblemStatus.PUBLISHED);
            when(adminQuestionService.publishQuestion(1L)).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/questions/1/publish")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("PUBLISHED")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/questions/{id}/archive - Archive Question")
    class ArchiveQuestion {

        @Test
        @DisplayName("Should archive question and return 200")
        void archiveQuestion_shouldReturnArchivedQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY, ProblemStatus.ARCHIVED);
            when(adminQuestionService.archiveQuestion(1L)).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/questions/1/archive")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("ARCHIVED")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/questions/{id}/unarchive - Unarchive Question")
    class UnarchiveQuestion {

        @Test
        @DisplayName("Should unarchive question and return 200")
        void unarchiveQuestion_shouldReturnUnarchivedQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY, ProblemStatus.PUBLISHED);
            when(adminQuestionService.unarchiveQuestion(1L)).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/questions/1/unarchive")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("PUBLISHED")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/questions/{id}/clone - Clone Question")
    class CloneQuestion {

        @Test
        @DisplayName("Should clone question and return 200")
        void cloneQuestion_shouldReturnClonedQuestion() throws Exception {
            AdminQuestionResponse response = createAdminQuestionResponse(2L, "Two Sum (Copy)", "two-sum-copy-123", Difficulty.EASY, ProblemStatus.DRAFT);
            when(adminQuestionService.cloneQuestion(eq(1L), eq(1L))).thenReturn(response);

            mockMvc.perform(post("/api/v1/admin/questions/1/clone")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .header("X-Admin-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.title", is("Two Sum (Copy)")))
                    .andExpect(jsonPath("$.status", is("DRAFT")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/questions/{id}/publish-check - Publish Check")
    class PublishCheck {

        @Test
        @DisplayName("Should return publish check result")
        void getPublishCheck_shouldReturnCheckResult() throws Exception {
            AdminQuestionService.PublishCheckResponse response = new AdminQuestionService.PublishCheckResponse(
                    true, List.of(), List.of("Consider adding more hints")
            );
            when(adminQuestionService.getPublishCheck(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/admin/questions/1/publish-check")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.canPublish", is(true)))
                    .andExpect(jsonPath("$.warnings[0]", is("Consider adding more hints")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/questions/stats - Stats")
    class Stats {

        @Test
        @DisplayName("Should return question statistics")
        void getStats_shouldReturnStats() throws Exception {
            AdminQuestionService.StatsResponse response = new AdminQuestionService.StatsResponse(100, 20, 70, 10);
            when(adminQuestionService.getStats()).thenReturn(response);

            mockMvc.perform(get("/api/v1/admin/questions/stats")
                            .header("X-Admin-Key", "dev-admin-key-change-in-production")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total", is(100)))
                    .andExpect(jsonPath("$.draft", is(20)))
                    .andExpect(jsonPath("$.published", is(70)))
                    .andExpect(jsonPath("$.archived", is(10)));
        }
    }
}