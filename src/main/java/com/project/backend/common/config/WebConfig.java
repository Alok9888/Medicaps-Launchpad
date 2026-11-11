package com.project.backend.common.config;

import com.project.backend.common.security.AdminKeyFilter;
import com.project.backend.common.security.InternalKeyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<AdminKeyFilter> adminKeyFilterRegistration(AdminKeyFilter filter) {
        FilterRegistrationBean<AdminKeyFilter> registration = new FilterRegistrationBean<>(filter);
        // Temporary admin guard until real Auth is implemented
        registration.addUrlPatterns("/api/v1/admin/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalKeyFilter> internalKeyFilterRegistration(InternalKeyFilter filter) {
        FilterRegistrationBean<InternalKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/v1/internal/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
