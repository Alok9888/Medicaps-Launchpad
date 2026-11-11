package com.project.backend.question.repository;

import com.project.backend.question.domain.ProblemCompany;
import com.project.backend.question.domain.ProblemCompany.ProblemCompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemCompanyRepository extends JpaRepository<ProblemCompany, ProblemCompanyId> {
    List<ProblemCompany> findByProblemId(Long problemId);
    List<ProblemCompany> findByCompanyId(Long companyId);

    long countByCompanyId(Long companyId);

    @Query("""
        SELECT c.id, c.name, COUNT(DISTINCT p.id)
        FROM Problem p
        JOIN p.problemCompanies pc
        JOIN pc.company c
        WHERE p.status = com.project.backend.question.domain.ProblemStatus.PUBLISHED
        GROUP BY c.id, c.name
        ORDER BY c.name
    """)
    List<Object[]> findPublishedCompanyCounts();

    @Query("""
        SELECT pc
        FROM ProblemCompany pc
        JOIN FETCH pc.company c
        WHERE pc.problem.id IN :problemIds
    """)
    List<ProblemCompany> findByProblemIds(@Param("problemIds") List<Long> problemIds);
}