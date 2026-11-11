package com.project.backend.question.repository;

import com.project.backend.question.domain.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByProblemIdOrderBySequenceOrderAsc(Long problemId);

    @Query("""
        SELECT tc
        FROM TestCase tc
        WHERE tc.problem.id = :problemId
        ORDER BY tc.sequenceOrder ASC
    """)
    List<TestCase> findAllByProblemId(@Param("problemId") Long problemId);
}