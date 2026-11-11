package com.project.backend.question.repository;

import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemStatus;
import com.project.backend.question.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>,
        JpaSpecificationExecutor<Problem> {

    Optional<Problem> findBySlugAndStatus(
            String slug,
            ProblemStatus status
    );

    Optional<Problem> findByIdAndStatus(
            Long id,
            ProblemStatus status
    );

    boolean existsBySlug(String slug);

    long countByStatus(ProblemStatus status);

    long countByTopicsContaining(Topic topic);

    @Query("""
        SELECT t.id, t.name, COUNT(DISTINCT p.id)
        FROM Problem p
        JOIN p.topics t
        WHERE p.status = com.project.backend.question.domain.ProblemStatus.PUBLISHED
        GROUP BY t.id, t.name
        ORDER BY t.name
    """)
    List<Object[]> findPublishedTopicCounts();

    @Query("""
        SELECT t.id, t.name
        FROM Problem p
        JOIN p.topics t
        WHERE p.id IN :problemIds
    """)
    List<Object[]> findTopicsByProblemIds(@Param("problemIds") List<Long> problemIds);
}