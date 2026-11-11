package com.project.backend.question.web;

import com.project.backend.question.dto.response.QuestionResponse;
import com.project.backend.question.dto.response.QuestionSummaryResponse;
import com.project.backend.question.dto.response.SampleTestCaseResponse;
import com.project.backend.question.domain.Difficulty;
import com.project.backend.question.service.QuestionService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@DisplayName("QuestionController Automated API Tests")
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    private QuestionResponse createSampleQuestionResponse(Long id, String title, String slug, Difficulty difficulty) {
        return new QuestionResponse(
                id,
                title,
                slug,
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
                difficulty,
                "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9",
                List.of("Array", "Hash Table"),
                List.of(
                        new SampleTestCaseResponse("nums = [2,7,11,15], target = 9", "[0,1]"),
                        new SampleTestCaseResponse("nums = [3,2,4], target = 6", "[1,2]")
                )
        );
    }

    private QuestionSummaryResponse createSummary(Long id, String title, String slug, Difficulty difficulty) {
        return new QuestionSummaryResponse(id, title, slug, difficulty);
    }

    @Nested
    @DisplayName("GET /api/v1/questions - List & Filter Questions")
    class GetQuestionsEndpoint {

        @Test
        @DisplayName("Should return 200 OK with default pagination when no params provided")
        void getQuestions_withDefaultParams_shouldReturnPagedQuestions() throws Exception {
            List<QuestionSummaryResponse> summaries = List.of(
                    createSummary(1L, "Two Sum", "two-sum", Difficulty.EASY),
                    createSummary(2L, "Add Two Numbers", "add-two-numbers", Difficulty.MEDIUM)
            );
            Page<QuestionSummaryResponse> page = new PageImpl<>(summaries);

            when(questionService.getQuestions(isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/questions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].id", is(1)))
                    .andExpect(jsonPath("$.content[0].title", is("Two Sum")))
                    .andExpect(jsonPath("$.content[0].slug", is("two-sum")))
                    .andExpect(jsonPath("$.content[0].difficulty", is("EASY")))
                    .andExpect(jsonPath("$.content[1].id", is(2)))
                    .andExpect(jsonPath("$.content[1].title", is("Add Two Numbers")))
                    .andExpect(jsonPath("$.content[1].slug", is("add-two-numbers")))
                    .andExpect(jsonPath("$.content[1].difficulty", is("MEDIUM")))
                    .andExpect(jsonPath("$.totalElements", is(2)));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(questionService).getQuestions(isNull(), isNull(), isNull(), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
            assertThat(capturedPageable.getPageSize()).isEqualTo(20);
            assertThat(capturedPageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(capturedPageable.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        }

        @Test
        @DisplayName("Should pass custom page and size parameters to service")
        void getQuestions_withCustomPagination_shouldPassToService() throws Exception {
            Page<QuestionSummaryResponse> emptyPage = new PageImpl<>(List.of());
            when(questionService.getQuestions(isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/questions")
                            .param("page", "3")
                            .param("size", "15")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(questionService).getQuestions(isNull(), isNull(), isNull(), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(3);
            assertThat(capturedPageable.getPageSize()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should cap page size at 100 when requested size exceeds limit")
        void getQuestions_whenSizeExceedsMax_shouldCapAt100() throws Exception {
            Page<QuestionSummaryResponse> emptyPage = new PageImpl<>(List.of());
            when(questionService.getQuestions(isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/questions")
                            .param("size", "250")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(questionService).getQuestions(isNull(), isNull(), isNull(), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should filter questions by difficulty")
        void getQuestions_withDifficultyFilter_shouldPassDifficultyToService() throws Exception {
            List<QuestionSummaryResponse> hardQuestions = List.of(
                    createSummary(10L, "Median of Two Sorted Arrays", "median-of-two-sorted-arrays", Difficulty.HARD)
            );
            Page<QuestionSummaryResponse> page = new PageImpl<>(hardQuestions);

            when(questionService.getQuestions(eq(Difficulty.HARD), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/questions")
                            .param("difficulty", "HARD")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].difficulty", is("HARD")));

            verify(questionService).getQuestions(eq(Difficulty.HARD), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter questions by topic")
        void getQuestions_withTopicFilter_shouldPassTopicToService() throws Exception {
            List<QuestionSummaryResponse> dpQuestions = List.of(
                    createSummary(5L, "Climbing Stairs", "climbing-stairs", Difficulty.EASY)
            );
            Page<QuestionSummaryResponse> page = new PageImpl<>(dpQuestions);

            when(questionService.getQuestions(isNull(), eq("dynamic-programming"), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/questions")
                            .param("topic", "dynamic-programming")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].slug", is("climbing-stairs")));

            verify(questionService).getQuestions(isNull(), eq("dynamic-programming"), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should filter questions by search term")
        void getQuestions_withSearchFilter_shouldPassSearchToService() throws Exception {
            List<QuestionSummaryResponse> matchingQuestions = List.of(
                    createSummary(1L, "Two Sum", "two-sum", Difficulty.EASY)
            );
            Page<QuestionSummaryResponse> page = new PageImpl<>(matchingQuestions);

            when(questionService.getQuestions(isNull(), isNull(), eq("Two Sum"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/questions")
                            .param("search", "Two Sum")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title", is("Two Sum")));

            verify(questionService).getQuestions(isNull(), isNull(), eq("Two Sum"), any(Pageable.class));
        }

        @Test
        @DisplayName("Should pass all combined filters simultaneously")
        void getQuestions_withAllFilters_shouldPassAllToService() throws Exception {
            Page<QuestionSummaryResponse> page = new PageImpl<>(List.of());

            when(questionService.getQuestions(eq(Difficulty.MEDIUM), eq("trees"), eq("binary"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/questions")
                            .param("difficulty", "MEDIUM")
                            .param("topic", "trees")
                            .param("search", "binary")
                            .param("page", "1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(questionService).getQuestions(
                    eq(Difficulty.MEDIUM),
                    eq("trees"),
                    eq("binary"),
                    pageableCaptor.capture()
            );

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(1);
            assertThat(captured.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when difficulty parameter is invalid")
        void getQuestions_withInvalidDifficulty_shouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/questions")
                            .param("difficulty", "NON_EXISTENT_DIFFICULTY")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(questionService);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/questions/{id} - Get Question by ID")
    class GetQuestionByIdEndpoint {

        @Test
        @DisplayName("Should return 200 OK with full question details when question exists")
        void getQuestionById_whenExists_shouldReturnQuestionDetails() throws Exception {
            QuestionResponse response = createSampleQuestionResponse(1L, "Two Sum", "two-sum", Difficulty.EASY);
            when(questionService.getQuestionById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/v1/questions/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("Two Sum")))
                    .andExpect(jsonPath("$.slug", is("two-sum")))
                    .andExpect(jsonPath("$.description", containsString("Given an array of integers")))
                    .andExpect(jsonPath("$.difficulty", is("EASY")))
                    .andExpect(jsonPath("$.constraints", containsString("2 <= nums.length")))
                    .andExpect(jsonPath("$.topics", hasSize(2)))
                    .andExpect(jsonPath("$.topics[0]", is("Array")))
                    .andExpect(jsonPath("$.topics[1]", is("Hash Table")))
                    .andExpect(jsonPath("$.sampleTestCases", hasSize(2)))
                    .andExpect(jsonPath("$.sampleTestCases[0].input", is("nums = [2,7,11,15], target = 9")))
                    .andExpect(jsonPath("$.sampleTestCases[0].expectedOutput", is("[0,1]")))
                    .andExpect(jsonPath("$.sampleTestCases[1].input", is("nums = [3,2,4], target = 6")))
                    .andExpect(jsonPath("$.sampleTestCases[1].expectedOutput", is("[1,2]")));

            verify(questionService).getQuestionById(1L);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when ID is not a numeric value")
        void getQuestionById_whenInvalidIdType_shouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/questions/not-a-number")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(questionService);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/questions/slug/{slug} - Get Question by Slug")
    class GetQuestionBySlugEndpoint {

        @Test
        @DisplayName("Should return 200 OK with full question details when slug exists")
        void getQuestionBySlug_whenExists_shouldReturnQuestionDetails() throws Exception {
            QuestionResponse response = createSampleQuestionResponse(42L, "Trapping Rain Water", "trapping-rain-water", Difficulty.HARD);
            when(questionService.getQuestionBySlug("trapping-rain-water")).thenReturn(response);

            mockMvc.perform(get("/api/v1/questions/slug/trapping-rain-water")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(42)))
                    .andExpect(jsonPath("$.title", is("Trapping Rain Water")))
                    .andExpect(jsonPath("$.slug", is("trapping-rain-water")))
                    .andExpect(jsonPath("$.difficulty", is("HARD")))
                    .andExpect(jsonPath("$.topics", hasItems("Array", "Hash Table")))
                    .andExpect(jsonPath("$.sampleTestCases", hasSize(2)));

            verify(questionService).getQuestionBySlug("trapping-rain-water");
        }
    }
}