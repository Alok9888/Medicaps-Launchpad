package com.project.backend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@ConfigurationProperties(prefix = "question")
@Validated
public class QuestionProperties {

    private Limits limits = new Limits();
    private Publish publish = new Publish();
    private Page page = new Page();

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }

    public Publish getPublish() {
        return publish;
    }

    public void setPublish(Publish publish) {
        this.publish = publish;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public static class Limits {
        @Min(100)
        private int minTimeMs = 100;

        @Max(10000)
        private int maxTimeMs = 10000;

        @Min(16384)
        private int minMemoryKb = 16384;

        @Max(524288)
        private int maxMemoryKb = 524288;

        public int getMinTimeMs() { return minTimeMs; }
        public void setMinTimeMs(int minTimeMs) { this.minTimeMs = minTimeMs; }
        public int getMaxTimeMs() { return maxTimeMs; }
        public void setMaxTimeMs(int maxTimeMs) { this.maxTimeMs = maxTimeMs; }
        public int getMinMemoryKb() { return minMemoryKb; }
        public void setMinMemoryKb(int minMemoryKb) { this.minMemoryKb = minMemoryKb; }
        public int getMaxMemoryKb() { return maxMemoryKb; }
        public void setMaxMemoryKb(int maxMemoryKb) { this.maxMemoryKb = maxMemoryKb; }
    }

    public static class Publish {
        @Min(1)
        private int minSampleCases = 1;

        @Min(3)
        private int minHiddenCases = 3;

        public int getMinSampleCases() { return minSampleCases; }
        public void setMinSampleCases(int minSampleCases) { this.minSampleCases = minSampleCases; }
        public int getMinHiddenCases() { return minHiddenCases; }
        public void setMinHiddenCases(int minHiddenCases) { this.minHiddenCases = minHiddenCases; }
    }

    public static class Page {
        @Positive
        @Max(100)
        private int maxSize = 100;

        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
    }
}