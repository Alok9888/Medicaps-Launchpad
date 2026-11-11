package com.project.backend.question.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_languages")
public class ProblemLanguage {

    @EmbeddedId
    private ProblemLanguageId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("problemId")
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("languageId")
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    protected ProblemLanguage() {
    }

    public ProblemLanguage(Problem problem, Language language, String starterCode, Boolean enabled) {
        this.id = new ProblemLanguageId(problem.getId(), language.getId());
        this.problem = problem;
        this.language = language;
        this.starterCode = starterCode;
        this.enabled = enabled;
    }

    public ProblemLanguageId getId() {
        return id;
    }

    public void setId(ProblemLanguageId id) {
        this.id = id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Embeddable
    public static class ProblemLanguageId implements java.io.Serializable {
        private Long problemId;
        private Long languageId;

        public ProblemLanguageId() {
        }

        public ProblemLanguageId(Long problemId, Long languageId) {
            this.problemId = problemId;
            this.languageId = languageId;
        }

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
        }

        public Long getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Long languageId) {
            this.languageId = languageId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProblemLanguageId that = (ProblemLanguageId) o;
            return java.util.Objects.equals(problemId, that.problemId) &&
                    java.util.Objects.equals(languageId, that.languageId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(problemId, languageId);
        }
    }
}