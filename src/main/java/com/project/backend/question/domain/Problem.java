package com.project.backend.question.domain;

import jakarta.persistence.*;

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
        @Index(name = "idx_problem_status", columnList = "status")
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

    public Set<Topic> getTopics() {
        return topics;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}