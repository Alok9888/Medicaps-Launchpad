package com.project.backend.question.service;

import com.project.backend.question.repository.ProblemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlugService Tests")
class SlugServiceTest {

    private final ProblemRepository problemRepository = Mockito.mock(ProblemRepository.class);
    private final SlugService service = new SlugService(problemRepository);

    @Nested
    @DisplayName("generateSlug")
    class GenerateSlug {

        @Test
        @DisplayName("Should generate slug from title")
        void generateSlug_fromTitle_shouldReturnSlug() {
            Mockito.when(problemRepository.existsBySlug("two-sum")).thenReturn(false);

            String slug = service.generateSlug("Two Sum");

            assertThat(slug).isEqualTo("two-sum");
        }

        @Test
        @DisplayName("Should handle special characters")
        void generateSlug_withSpecialChars_shouldSanitize() {
            Mockito.when(problemRepository.existsBySlug("hello-world")).thenReturn(false);

            String slug = service.generateSlug("Hello World!");

            assertThat(slug).isEqualTo("hello-world");
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void generateSlug_withUnicode_shouldNormalize() {
            Mockito.when(problemRepository.existsBySlug("cafe")).thenReturn(false);

            String slug = service.generateSlug("Café");

            assertThat(slug).isEqualTo("cafe");
        }

        @Test
        @DisplayName("Should handle multiple spaces")
        void generateSlug_withMultipleSpaces_shouldCollapse() {
            Mockito.when(problemRepository.existsBySlug("a-b-c")).thenReturn(false);

            String slug = service.generateSlug("A   B   C");

            assertThat(slug).isEqualTo("a-b-c");
        }

        @Test
        @DisplayName("Should handle leading/trailing spaces")
        void generateSlug_withLeadingTrailingSpaces_shouldTrim() {
            Mockito.when(problemRepository.existsBySlug("trimmed")).thenReturn(false);

            String slug = service.generateSlug("  Trimmed  ");

            assertThat(slug).isEqualTo("trimmed");
        }

        @Test
        @DisplayName("Should handle empty title")
        void generateSlug_emptyTitle_shouldReturnDefault() {
            Mockito.when(problemRepository.existsBySlug(Mockito.startsWith("untitled-"))).thenReturn(false);

            String slug = service.generateSlug("");
            String slug2 = service.generateSlug("   ");

            assertThat(slug).startsWith("untitled-");
            assertThat(slug2).startsWith("untitled-");
        }

        @Test
        @DisplayName("Should make unique when slug exists")
        void generateSlug_whenExists_shouldMakeUnique() {
            Mockito.when(problemRepository.existsBySlug("two-sum")).thenReturn(true);
            Mockito.when(problemRepository.existsBySlug("two-sum-1")).thenReturn(false);

            String slug = service.generateSlug("Two Sum");

            assertThat(slug).isEqualTo("two-sum-1");
        }

        @Test
        @DisplayName("Should increment counter for multiple collisions")
        void generateSlug_multipleCollisions_shouldIncrement() {
            Mockito.when(problemRepository.existsBySlug("test")).thenReturn(true);
            Mockito.when(problemRepository.existsBySlug("test-1")).thenReturn(true);
            Mockito.when(problemRepository.existsBySlug("test-2")).thenReturn(false);

            String slug = service.generateSlug("Test");

            assertThat(slug).isEqualTo("test-2");
        }

        @Test
        @DisplayName("Should truncate long slugs")
        void generateSlug_longTitle_shouldTruncate() {
            String longTitle = "A".repeat(300);
            String expectedBase = "a".repeat(210); // MAX_SLUG_LENGTH - 10 = 210
            Mockito.when(problemRepository.existsBySlug(expectedBase)).thenReturn(false);

            String slug = service.generateSlug(longTitle);

            assertThat(slug.length()).isLessThanOrEqualTo(220);
            assertThat(slug).startsWith(expectedBase.substring(0, 200));
        }
    }

    @Nested
    @DisplayName("makeUnique")
    class MakeUnique {

        @Test
        @DisplayName("Should return base slug when unique")
        void makeUnique_whenUnique_shouldReturnBase() {
            Mockito.when(problemRepository.existsBySlug("unique-slug")).thenReturn(false);

            String result = service.makeUnique("unique-slug");

            assertThat(result).isEqualTo("unique-slug");
        }

        @Test
        @DisplayName("Should append counter when exists")
        void makeUnique_whenExists_shouldAppendCounter() {
            Mockito.when(problemRepository.existsBySlug("slug")).thenReturn(true);
            Mockito.when(problemRepository.existsBySlug("slug-1")).thenReturn(false);

            String result = service.makeUnique("slug");

            assertThat(result).isEqualTo("slug-1");
        }
    }

    @Nested
    @DisplayName("isValidSlug")
    class IsValidSlug {

        @Test
        @DisplayName("Should return true for valid slug")
        void isValidSlug_validSlug_shouldReturnTrue() {
            assertThat(service.isValidSlug("two-sum")).isTrue();
            assertThat(service.isValidSlug("abc123")).isTrue();
            assertThat(service.isValidSlug("a-b-c-1-2-3")).isTrue();
        }

        @Test
        @DisplayName("Should return false for invalid slug")
        void isValidSlug_invalidSlug_shouldReturnFalse() {
            assertThat(service.isValidSlug("Two-Sum")).isFalse(); // uppercase
            assertThat(service.isValidSlug("two_sum")).isFalse(); // underscore
            assertThat(service.isValidSlug("-two-sum")).isFalse(); // leading hyphen
            assertThat(service.isValidSlug("two-sum-")).isFalse(); // trailing hyphen
            assertThat(service.isValidSlug("two--sum")).isFalse(); // double hyphen
            assertThat(service.isValidSlug("")).isFalse(); // empty
            assertThat(service.isValidSlug(null)).isFalse(); // null
        }
    }

    @Nested
    @DisplayName("sanitizeSlug")
    class SanitizeSlug {

        @Test
        @DisplayName("Should sanitize slug")
        void sanitizeSlug_shouldClean() {
            assertThat(service.sanitizeSlug("Two-Sum")).isEqualTo("two-sum");
            assertThat(service.sanitizeSlug("two_sum")).isEqualTo("twosum"); // underscores removed
            assertThat(service.sanitizeSlug("-two-sum-")).isEqualTo("two-sum");
            assertThat(service.sanitizeSlug("two--sum")).isEqualTo("two-sum");
        }

        @Test
        @DisplayName("Should return null for empty")
        void sanitizeSlug_empty_shouldReturnNull() {
            assertThat(service.sanitizeSlug("")).isNull();
            assertThat(service.sanitizeSlug("   ")).isNull();
            assertThat(service.sanitizeSlug(null)).isNull();
        }
    }
}