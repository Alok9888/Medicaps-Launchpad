package com.project.backend.question.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_companies")
public class ProblemCompany {

    @EmbeddedId
    private ProblemCompanyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("problemId")
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private Integer frequency;

    protected ProblemCompany() {
    }

    public ProblemCompany(Problem problem, Company company, Integer frequency) {
        this.id = new ProblemCompanyId(problem.getId(), company.getId());
        this.problem = problem;
        this.company = company;
        this.frequency = frequency;
    }

    public ProblemCompanyId getId() {
        return id;
    }

    public void setId(ProblemCompanyId id) {
        this.id = id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    @Embeddable
    public static class ProblemCompanyId implements java.io.Serializable {
        private Long problemId;
        private Long companyId;

        public ProblemCompanyId() {
        }

        public ProblemCompanyId(Long problemId, Long companyId) {
            this.problemId = problemId;
            this.companyId = companyId;
        }

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProblemCompanyId that = (ProblemCompanyId) o;
            return java.util.Objects.equals(problemId, that.problemId) &&
                    java.util.Objects.equals(companyId, that.companyId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(problemId, companyId);
        }
    }
}