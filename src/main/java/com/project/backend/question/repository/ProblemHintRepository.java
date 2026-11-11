package com.project.backend.question.repository;

import com.project.backend.question.domain.ProblemHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemHintRepository extends JpaRepository<ProblemHint, Long> {
    List<ProblemHint> findByProblemIdOrderByHintOrderAsc(Long problemId);
    Optional<ProblemHint> findByProblemIdAndHintOrder(Long problemId, Integer hintOrder);
}