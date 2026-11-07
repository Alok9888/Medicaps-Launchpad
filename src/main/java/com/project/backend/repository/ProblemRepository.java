
package com.project.backend.repository;

import com.project.backend.model.Problem;
import com.project.backend.model.ProblemStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long>,
        JpaSpecificationExecutor<Problem> {

    // Find a question by its slug and status
    Optional<Problem> findBySlugAndStatus(
            String slug,
            ProblemStatus status
    );

    // Find a question by its ID and status
    Optional<Problem> findByIdAndStatus(
            Long id,
            ProblemStatus status
    );
}
