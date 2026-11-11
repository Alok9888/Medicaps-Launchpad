package com.project.backend.question.service;

import com.project.backend.common.config.QuestionProperties;
import com.project.backend.question.domain.Company;
import com.project.backend.question.domain.Difficulty;
import com.project.backend.question.domain.Language;
import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemCompany;
import com.project.backend.question.domain.ProblemHint;
import com.project.backend.question.domain.ProblemLanguage;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.domain.TestCase;
import com.project.backend.question.domain.Topic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PublishCheckService Tests")
class PublishCheckServiceTest {

    private final QuestionProperties properties = createProperties();
    private final PublishCheckService service = new PublishCheckService(properties);

    private QuestionProperties createProperties() {
        QuestionProperties props = Mockito.mock(QuestionProperties.class);
        QuestionProperties.Limits limits = Mockito.mock(QuestionProperties.Limits.class);
        QuestionProperties.Publish publish = Mockito.mock(QuestionProperties.Publish.class);

        Mockito.when(props.getLimits()).thenReturn(limits);
        Mockito.when(props.getPublish()).thenReturn(publish);

        Mockito.when(limits.getMinTimeMs()).thenReturn(100);
        Mockito.when(limits.getMaxTimeMs()).thenReturn(10000);
        Mockito.when(limits.getMinMemoryKb()).thenReturn(16384);
        Mockito.when(limits.getMaxMemoryKb()).thenReturn(524288);

        Mockito.when(publish.getMinSampleCases()).thenReturn(1);
        Mockito.when(publish.getMinHiddenCases()).thenReturn(3);

        return props;
    }

    private Problem createValidProblem() {
        Problem problem = new Problem(
                "Two Sum",
                "two-sum",
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. You may assume that each input would have exactly one solution, and you may not use the same element twice. You can return the answer in any order.",
                Difficulty.EASY,
                "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9\nOnly one valid answer exists."
        );
        problem.setInputFormat("Array of integers and target integer");
        problem.setOutputFormat("Array of two indices");
        problem.setTimeLimitMs(2000);
        problem.setMemoryLimitKb(262144);
        problem.setComparator("TRIM_LINES");
        problem.setStatus(ProblemStatus.DRAFT);

        // Topics
        Topic topic = new Topic("Array");
                problem.addTopic(topic);

        // Companies
        Company company = new Company("Google");
                ProblemCompany pc = new ProblemCompany(problem, company, 5);
        problem.addProblemCompany(pc);

        // Test Cases
        problem.addTestCase(new TestCase("[2,7,11,15]", "[0,1]", false, 0));
        problem.addTestCase(new TestCase("[3,2,4]", "[1,2]", true, 1));
        problem.addTestCase(new TestCase("[3,3]", "[0,1]", true, 2));
        problem.addTestCase(new TestCase("[1,2,3,4,5]", "[0,2]", true, 3));

        // Languages
        Language language = new Language("Python", 71, "py", true);
                ProblemLanguage pl = new ProblemLanguage(problem, language, "def twoSum(nums, target):\n    pass", true);
        problem.addProblemLanguage(pl);

        return problem;
    }

    @Nested
    @DisplayName("Valid Problem")
    class ValidProblem {

        @Test
        @DisplayName("Should return canPublish=true for valid problem")
        void validProblem_shouldReturnCanPublish() {
            Problem problem = createValidProblem();

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.errors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Required Fields Validation")
    class RequiredFields {

        @Test
        @DisplayName("Should fail when title is missing")
        void missingTitle_shouldError() {
            Problem problem = createValidProblem();
            problem.setTitle(null);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Title is required");
        }

        @Test
        @DisplayName("Should fail when slug is missing")
        void missingSlug_shouldError() {
            Problem problem = createValidProblem();
            problem.setSlug("");

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Slug is required");
        }

        @Test
        @DisplayName("Should fail when description is missing")
        void missingDescription_shouldError() {
            Problem problem = createValidProblem();
            problem.setDescription("");

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Description is required");
        }

        @Test
        @DisplayName("Should fail when difficulty is missing")
        void missingDifficulty_shouldError() {
            Problem problem = createValidProblem();
            problem.setDifficulty(null);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Difficulty is required");
        }

        @Test
        @DisplayName("Should fail when time limit is missing")
        void missingTimeLimit_shouldError() {
            Problem problem = createValidProblem();
            problem.setTimeLimitMs(null);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Time limit is required");
        }

        @Test
        @DisplayName("Should fail when memory limit is missing")
        void missingMemoryLimit_shouldError() {
            Problem problem = createValidProblem();
            problem.setMemoryLimitKb(null);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Memory limit is required");
        }

        @Test
        @DisplayName("Should fail when comparator is missing")
        void missingComparator_shouldError() {
            Problem problem = createValidProblem();
            problem.setComparator("");

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Comparator is required");
        }
    }

    @Nested
    @DisplayName("Limits Validation")
    class LimitsValidation {

        @Test
        @DisplayName("Should fail when time limit below minimum")
        void timeLimitBelowMin_shouldError() {
            Problem problem = createValidProblem();
            problem.setTimeLimitMs(50);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Time limit must be at least 100ms");
        }

        @Test
        @DisplayName("Should fail when time limit above maximum")
        void timeLimitAboveMax_shouldError() {
            Problem problem = createValidProblem();
            problem.setTimeLimitMs(20000);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Time limit must not exceed 10000ms");
        }

        @Test
        @DisplayName("Should fail when memory limit below minimum")
        void memoryLimitBelowMin_shouldError() {
            Problem problem = createValidProblem();
            problem.setMemoryLimitKb(8000);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Memory limit must be at least 16384KB");
        }

        @Test
        @DisplayName("Should fail when memory limit above maximum")
        void memoryLimitAboveMax_shouldError() {
            Problem problem = createValidProblem();
            problem.setMemoryLimitKb(600000);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("Memory limit must not exceed 524288KB");
        }
    }

    @Nested
    @DisplayName("Topics and Companies Validation")
    class TopicsAndCompanies {

        @Test
        @DisplayName("Should fail when no topics")
        void noTopics_shouldError() {
            Problem problem = createValidProblem();
            problem.getTopics().clear();

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("At least one topic is required");
        }

        @Test
        @DisplayName("Should fail when no companies")
        void noCompanies_shouldError() {
            Problem problem = createValidProblem();
            problem.getProblemCompanies().clear();

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("At least one company is required");
        }
    }

    @Nested
    @DisplayName("Test Cases Validation")
    class TestCasesValidation {

        @Test
        @DisplayName("Should fail when insufficient sample cases")
        void insufficientSampleCases_shouldError() {
            Problem problem = createValidProblem();
            problem.getTestCases().clear();
            problem.addTestCase(new TestCase("[2,7,11,15]", "[0,1]", true, 0));
            problem.addTestCase(new TestCase("[3,2,4]", "[1,2]", true, 1));
            problem.addTestCase(new TestCase("[3,3]", "[0,1]", true, 2));

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("At least 1 sample test case(s) required (found 0)");
        }

        @Test
        @DisplayName("Should fail when insufficient hidden cases")
        void insufficientHiddenCases_shouldError() {
            Problem problem = createValidProblem();
            problem.getTestCases().clear();
            problem.addTestCase(new TestCase("[2,7,11,15]", "[0,1]", false, 0));

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("At least 3 hidden test case(s) required (found 0)");
        }

        @Test
        @DisplayName("Should warn when sequence order has gaps")
        void sequenceGaps_shouldWarn() {
            Problem problem = createValidProblem();
            problem.getTestCases().clear();
            problem.addTestCase(new TestCase("[2,7,11,15]", "[0,1]", false, 0));
            problem.addTestCase(new TestCase("[3,2,4]", "[1,2]", true, 2)); // gap at 1
            problem.addTestCase(new TestCase("[3,3]", "[0,1]", true, 3));
            problem.addTestCase(new TestCase("[1,2,3,4,5]", "[0,2]", true, 4));

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("sequence order has gaps"));
        }

        @Test
        @DisplayName("Should warn when duplicate test case inputs")
        void duplicateInputs_shouldWarn() {
            Problem problem = createValidProblem();
            problem.getTestCases().clear();
            problem.addTestCase(new TestCase("[2,7,11,15]", "[0,1]", false, 0));
            problem.addTestCase(new TestCase("[2,7,11,15]", "[0,1]", true, 1));
            problem.addTestCase(new TestCase("[3,3]", "[0,1]", true, 2));
            problem.addTestCase(new TestCase("[1,2,3,4,5]", "[0,2]", true, 3));

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Duplicate test case input"));
        }

        @Test
        @DisplayName("Should warn when hidden cases missing explanation")
        void hiddenWithoutExplanation_shouldWarn() {
            Problem problem = createValidProblem();
            problem.getTestCases().clear();
            TestCase tc1 = new TestCase("[2,7,11,15]", "[0,1]", false, 0);
            TestCase tc2 = new TestCase("[3,2,4]", "[1,2]", true, 1);
            tc2.setExplanation("");
            TestCase tc3 = new TestCase("[3,3]", "[0,1]", true, 2);
            tc3.setExplanation("Explanation");
            TestCase tc4 = new TestCase("[1,2,3,4,5]", "[0,2]", true, 3);
            tc4.setExplanation("Explanation");
            problem.addTestCase(tc1);
            problem.addTestCase(tc2);
            problem.addTestCase(tc3);
            problem.addTestCase(tc4);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("hidden test case(s) missing explanation"));
        }
    }

    @Nested
    @DisplayName("Languages Validation")
    class LanguagesValidation {

        @Test
        @DisplayName("Should fail when no languages")
        void noLanguages_shouldError() {
            Problem problem = createValidProblem();
            problem.getProblemLanguages().clear();

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("At least one language configuration required");
        }

        @Test
        @DisplayName("Should fail when no enabled languages")
        void noEnabledLanguages_shouldError() {
            Problem problem = createValidProblem();
            problem.getProblemLanguages().clear();

            Language language = new Language("Python", 71, "py", true);
                        ProblemLanguage pl = new ProblemLanguage(problem, language, "def twoSum(nums, target):\n    pass", false);
            problem.addProblemLanguage(pl);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).contains("At least one enabled language required");
        }
    }

    @Nested
    @DisplayName("Hints Validation")
    class HintsValidation {

        @Test
        @DisplayName("Should warn when no hints")
        void noHints_shouldWarn() {
            Problem problem = createValidProblem();
            problem.getHints().clear();

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("No hints configured"));
        }

        @Test
        @DisplayName("Should add info when hints present")
        void hasHints_shouldInfo() {
            Problem problem = createValidProblem();
            ProblemHint hint = new ProblemHint(problem, 1, "First hint");
            problem.addHint(hint);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.info()).anyMatch(i -> i.contains("Hints: 1"));
        }
    }

    @Nested
    @DisplayName("Slug Format")
    class SlugFormat {

        @Test
        @DisplayName("Should warn when slug format invalid")
        void invalidSlug_shouldWarn() {
            Problem problem = createValidProblem();
            problem.setSlug("Two-Sum"); // uppercase

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Slug should be lowercase"));
        }

        @Test
        @DisplayName("Should pass when slug format valid")
        void validSlug_shouldPass() {
            Problem problem = createValidProblem();
            problem.setSlug("two-sum");

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).noneMatch(w -> w.contains("Slug should be lowercase"));
        }
    }

    @Nested
    @DisplayName("Content Quality")
    class ContentQuality {

        @Test
        @DisplayName("Should warn when description too short")
        void shortDescription_shouldWarn() {
            Problem problem = createValidProblem();
            problem.setDescription("Short desc"); // ~2 words

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Description seems short"));
        }

        @Test
        @DisplayName("Should warn when description too long")
        void longDescription_shouldWarn() {
            Problem problem = createValidProblem();
            String longDesc = "word ".repeat(6000); // ~6000 words
            problem.setDescription(longDesc);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Description is very long"));
        }

        @Test
        @DisplayName("Should warn when constraints minimal")
        void minimalConstraints_shouldWarn() {
            Problem problem = createValidProblem();
            problem.setProblemConstraints("Line 1\nLine 2"); // only 2 lines

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("Constraints seem minimal"));
        }

        @Test
        @DisplayName("Should warn when memory-to-time ratio high")
        void highRatio_shouldWarn() {
            Problem problem = createValidProblem();
            problem.setTimeLimitMs(100);
            problem.setMemoryLimitKb(100000); // ratio = 1000

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isTrue();
            assertThat(result.warnings()).anyMatch(w -> w.contains("High memory-to-time ratio"));
        }
    }

    @Nested
    @DisplayName("Already Published")
    class AlreadyPublished {

        @Test
        @DisplayName("Should return false when already published")
        void alreadyPublished_shouldReturnFalse() {
            Problem problem = createValidProblem();
            problem.setStatus(ProblemStatus.PUBLISHED);

            PublishCheckService.PublishCheckResult result = service.check(problem);

            assertThat(result.canPublish()).isFalse();
            assertThat(result.errors()).isEmpty();
        }
    }
}