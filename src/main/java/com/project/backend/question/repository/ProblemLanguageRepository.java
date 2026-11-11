package com.project.backend.question.repository;

import com.project.backend.question.domain.ProblemLanguage;
import com.project.backend.question.domain.ProblemLanguage.ProblemLanguageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemLanguageRepository extends JpaRepository<ProblemLanguage, ProblemLanguageId> {
    Optional<ProblemLanguage> findByProblemIdAndLanguageId(Long problemId, Long languageId);
    List<ProblemLanguage> findByProblemId(Long problemId);
    List<ProblemLanguage> findByProblemIdAndEnabledTrue(Long problemId);
}