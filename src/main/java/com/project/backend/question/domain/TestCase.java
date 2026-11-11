package com.project.backend.question.domain;

import jakarta.persistence.*;

@Entity
@Table(
    name = "test_cases",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_test_case_problem_order",
            columnNames = {"problem_id", "sequence_order"}
        )
    }
)
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    protected TestCase() {
    }

    public TestCase(
            String input,
            String expectedOutput,
            boolean hidden,
            Integer sequenceOrder
    ) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.hidden = hidden;
        this.sequenceOrder = sequenceOrder;
    }

    public Long getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    void setProblem(Problem problem) {
        this.problem = problem;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}