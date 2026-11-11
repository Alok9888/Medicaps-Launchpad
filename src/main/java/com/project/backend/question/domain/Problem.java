package com.project.backend.question.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
    name = "problems",
    indexes = {
        @Index(name = "idx_problem_difficulty", columnList = "difficulty"),
        @Index(name = "idx_problem_status", columnList = "status"),
        @Index(name = "idx_problem_status_difficulty", columnList = "status, difficulty")
    }
)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProblemStatus status = ProblemStatus.DRAFT;

    @Column(name = "problem_constraints", columnDefinition = "TEXT")
    private String problemConstraints;

    @Column(name = "input_format", columnDefinition = "TEXT")
    private String inputFormat;

    @Column(name = "output_format", columnDefinition = "TEXT")
    private String outputFormat;

    @Column(name = "time_limit_ms", nullable = false)
    private Integer timeLimitMs = 2000;

    @Column(name = "memory_limit_kb", nullable = false)
    private Integer memoryLimitKb = 262144;

    @Column(name = "comparator", nullable = false, length = 20)
    private String comparator = "TRIM_LINES";

    @Column(name = "float_epsilon", precision = 12, scale = 10)
    private BigDecimal floatEpsilon;

    @Column(name = "content_revision", nullable = false)
    private Integer contentRevision = 1;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "problem_topics",
        joinColumns = @JoinColumn(name = "problem_id"),
        inverseJoinColumns = @JoinColumn(name = "topic_id"),
        uniqueConstraints = @UniqueConstraint(
            name = "uk_problem_topic",
            columnNames = {"problem_id", "topic_id"}
        )
    )
    private Set<Topic> topics = new HashSet<>();

    @OneToMany(
        mappedBy = "problem",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("sequenceOrder ASC")
    private List<TestCase> testCases = new ArrayList<>();

    @OneToMany(
        mappedBy = "problem",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("hintOrder ASC")
    private List<ProblemHint> hints = new ArrayList<>();

    @OneToMany(
        mappedBy = "problem",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProblemLanguage> problemLanguages = new ArrayList<>();

    @OneToMany(
        mappedBy = "problem",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProblemCompany> problemCompanies = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Problem() {
    }

    public Problem(
            String title,
            String slug,
            String description,
            Difficulty difficulty,
            String problemConstraints
    ) {
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.difficulty = difficulty;
        this.problemConstraints = problemConstraints;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        topics.remove(topic);
    }

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
        testCase.setProblem(this);
    }

    public void removeTestCase(TestCase testCase) {
        testCases.remove(testCase);
        testCase.setProblem(null);
    }

    public void addHint(ProblemHint hint) {
        hints.add(hint);
        hint.setProblem(this);
    }

    public void removeHint(ProblemHint hint) {
        hints.remove(hint);
        hint.setProblem(null);
    }

    public void addProblemLanguage(ProblemLanguage pl) {
        problemLanguages.add(pl);
        pl.setProblem(this);
    }

    public void removeProblemLanguage(ProblemLanguage pl) {
        problemLanguages.remove(pl);
        pl.setProblem(null);
    }

    public void addProblemCompany(ProblemCompany pc) {
        problemCompanies.add(pc);
        pc.setProblem(this);
    }

    public void removeProblemCompany(ProblemCompany pc) {
        problemCompanies.remove(pc);
        pc.setProblem(null);
    }

    public void publish() {
        if (this.status == ProblemStatus.DRAFT || this.status == ProblemStatus.ARCHIVED) {
            this.status = ProblemStatus.PUBLISHED;
            if (this.publishedAt == null) {
                this.publishedAt = Instant.now();
            }
        } else {
            throw new IllegalStateException("Cannot publish from status: " + this.status);
        }
    }

    public void archive() {
        if (this.status == ProblemStatus.PUBLISHED) {
            this.status = ProblemStatus.ARCHIVED;
        } else {
            throw new IllegalStateException("Cannot archive from status: " + this.status);
        }
    }

    public void unarchive() {
        if (this.status == ProblemStatus.ARCHIVED) {
            this.status = ProblemStatus.PUBLISHED;
            if (this.publishedAt == null) {
                this.publishedAt = Instant.now();
            }
        } else {
            throw new IllegalStateException("Cannot unarchive from status: " + this.status);
        }
    }

    public void incrementContentRevision() {
        this.contentRevision++;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public ProblemStatus getStatus() {
        return status;
    }

    public void setStatus(ProblemStatus status) {
        this.status = status;
    }

    public String getProblemConstraints() {
        return problemConstraints;
    }

    public void setProblemConstraints(String problemConstraints) {
        this.problemConstraints = problemConstraints;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public Integer getMemoryLimitKb() {
        return memoryLimitKb;
    }

    public void setMemoryLimitKb(Integer memoryLimitKb) {
        this.memoryLimitKb = memoryLimitKb;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public BigDecimal getFloatEpsilon() {
        return floatEpsilon;
    }

    public void setFloatEpsilon(BigDecimal floatEpsilon) {
        this.floatEpsilon = floatEpsilon;
    }

    public Integer getContentRevision() {
        return contentRevision;
    }

    public void setContentRevision(Integer contentRevision) {
        this.contentRevision = contentRevision;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(Long rowVersion) {
        this.rowVersion = rowVersion;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public List<ProblemHint> getHints() {
        return hints;
    }

    public List<ProblemLanguage> getProblemLanguages() {
        return problemLanguages;
    }

    public List<ProblemCompany> getProblemCompanies() {
        return problemCompanies;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}