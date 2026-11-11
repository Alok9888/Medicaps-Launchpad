package com.project.backend.question.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "languages")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "judge0_language_id", nullable = false)
    private Integer judge0LanguageId;

    @Column(name = "file_extension", nullable = false, length = 20)
    private String fileExtension;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    protected Language() {
    }

    public Language(String name, Integer judge0LanguageId, String fileExtension, Boolean isActive) {
        this.name = name;
        this.judge0LanguageId = judge0LanguageId;
        this.fileExtension = fileExtension;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getJudge0LanguageId() {
        return judge0LanguageId;
    }

    public void setJudge0LanguageId(Integer judge0LanguageId) {
        this.judge0LanguageId = judge0LanguageId;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}