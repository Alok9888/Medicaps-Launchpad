package com.project.backend.question.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_hints")
public class ProblemHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "hint_order", nullable = false)
    private Integer hintOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ProblemHint() {
    }

    public ProblemHint(Problem problem, Integer hintOrder, String content) {
        this.problem = problem;
        this.hintOrder = hintOrder;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Integer getHintOrder() {
        return hintOrder;
    }

    public void setHintOrder(Integer hintOrder) {
        this.hintOrder = hintOrder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}