package com.project.backend.question.repository;

import com.project.backend.question.domain.Problem;
import com.project.backend.question.domain.ProblemStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

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
}