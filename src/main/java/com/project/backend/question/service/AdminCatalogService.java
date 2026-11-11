package com.project.backend.question.service;

import com.project.backend.common.exception.ConflictException;
import com.project.backend.common.exception.NotFoundException;
import com.project.backend.common.exception.ValidationException;
import com.project.backend.question.domain.Company;
import com.project.backend.question.domain.Language;
import com.project.backend.question.domain.Topic;
import com.project.backend.question.dto.request.CompanyCreateRequest;
import com.project.backend.question.dto.request.LanguageCreateRequest;
import com.project.backend.question.dto.request.TopicCreateRequest;
import com.project.backend.question.dto.response.CompanySummary;
import com.project.backend.question.dto.response.LanguageSummary;
import com.project.backend.question.dto.response.TopicSummary;
import com.project.backend.question.repository.CompanyRepository;
import com.project.backend.question.repository.LanguageRepository;
import com.project.backend.question.repository.ProblemCompanyRepository;
import com.project.backend.question.repository.ProblemRepository;
import com.project.backend.question.repository.TopicRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCatalogService {

    private final TopicRepository topicRepository;
    private final CompanyRepository companyRepository;
    private final LanguageRepository languageRepository;
    private final ProblemRepository problemRepository;
    private final ProblemCompanyRepository problemCompanyRepository;

    public AdminCatalogService(
            TopicRepository topicRepository,
            CompanyRepository companyRepository,
            LanguageRepository languageRepository,
            ProblemRepository problemRepository,
            ProblemCompanyRepository problemCompanyRepository
    ) {
        this.topicRepository = topicRepository;
        this.companyRepository = companyRepository;
        this.languageRepository = languageRepository;
        this.problemRepository = problemRepository;
        this.problemCompanyRepository = problemCompanyRepository;
    }

    // Topics
    @Transactional
    public TopicSummary createTopic(TopicCreateRequest request) {
        if (topicRepository.existsByName(request.name())) {
            throw new ConflictException("DUPLICATE_NAME", "Topic with this name already exists");
        }
        Topic topic = new Topic(request.name());
        Topic saved = topicRepository.save(topic);
        return new TopicSummary(saved.getId(), saved.getName(), 0L);
    }

    @Transactional
    public TopicSummary updateTopic(Long id, TopicCreateRequest request) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "Topic not found"));

        if (!topic.getName().equals(request.name()) && topicRepository.existsByName(request.name())) {
            throw new ConflictException("DUPLICATE_NAME", "Topic with this name already exists");
        }

        topic.setName(request.name());
        Topic saved = topicRepository.save(topic);
        return new TopicSummary(saved.getId(), saved.getName(), 0L);
    }

    @Transactional
    public void deleteTopic(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "Topic not found"));

        long count = problemRepository.countByTopicsContaining(topic);
        if (count > 0) {
            throw new ConflictException("CATALOG_IN_USE", "Cannot delete topic: used by " + count + " question(s)");
        }

        topicRepository.delete(topic);
    }

    public List<TopicSummary> getAllTopics() {
        return topicRepository.findAllByOrderByNameAsc().stream()
                .map(t -> new TopicSummary(t.getId(), t.getName(), 0L))
                .collect(Collectors.toList());
    }

    // Companies
    @Transactional
    public CompanySummary createCompany(CompanyCreateRequest request) {
        if (companyRepository.existsByName(request.name())) {
            throw new ConflictException("DUPLICATE_NAME", "Company with this name already exists");
        }
        Company company = new Company(request.name());
        Company saved = companyRepository.save(company);
        return new CompanySummary(saved.getId(), saved.getName(), 0);
    }

    @Transactional
    public CompanySummary updateCompany(Long id, CompanyCreateRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("COMPANY_NOT_FOUND", "Company not found"));

        if (!company.getName().equals(request.name()) && companyRepository.existsByName(request.name())) {
            throw new ConflictException("DUPLICATE_NAME", "Company with this name already exists");
        }

        company.setName(request.name());
        Company saved = companyRepository.save(company);
        return new CompanySummary(saved.getId(), saved.getName(), 0);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("COMPANY_NOT_FOUND", "Company not found"));

        long count = problemCompanyRepository.countByCompanyId(id);
        if (count > 0) {
            throw new ConflictException("CATALOG_IN_USE", "Cannot delete company: used by " + count + " question(s)");
        }

        companyRepository.delete(company);
    }

    public List<CompanySummary> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(c -> new CompanySummary(c.getId(), c.getName(), 0))
                .collect(Collectors.toList());
    }

    // Languages
    @Transactional
    public LanguageSummary createLanguage(LanguageCreateRequest request) {
        if (languageRepository.existsByName(request.name())) {
            throw new ConflictException("DUPLICATE_NAME", "Language with this name already exists");
        }
        Language language = new Language(
                request.name(),
                request.judge0LanguageId(),
                request.fileExtension(),
                request.isActive()
        );
        Language saved = languageRepository.save(language);
        return new LanguageSummary(saved.getId(), saved.getName(), saved.getFileExtension());
    }

    @Transactional
    public LanguageSummary updateLanguage(Long id, LanguageCreateRequest request) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not found"));

        if (!language.getName().equals(request.name()) && languageRepository.existsByName(request.name())) {
            throw new ConflictException("DUPLICATE_NAME", "Language with this name already exists");
        }

        language.setName(request.name());
        language.setJudge0LanguageId(request.judge0LanguageId());
        language.setFileExtension(request.fileExtension());
        language.setIsActive(request.isActive());

        Language saved = languageRepository.save(language);
        return new LanguageSummary(saved.getId(), saved.getName(), saved.getFileExtension());
    }

    @Transactional
    public void deleteLanguage(Long id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LANGUAGE_NOT_FOUND", "Language not found"));

        // Note: We don't check ProblemLanguage here as it's a join table with composite key
        // In practice, you'd want to check if any ProblemLanguage references this language
        languageRepository.delete(language);
    }

    public List<LanguageSummary> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(l -> new LanguageSummary(l.getId(), l.getName(), l.getFileExtension()))
                .collect(Collectors.toList());
    }
}