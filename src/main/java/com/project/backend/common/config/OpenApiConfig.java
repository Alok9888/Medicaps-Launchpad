package com.project.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Medicaps Launchpad - Question Bank API")
                        .version("v1")
                        .description("""
                                REST API for the Medicaps Launchpad Question Bank module.

                                ## Authentication
                                - **Student APIs** (`/api/v1/questions/**`, `/api/v1/topics`, `/api/v1/companies`, `/api/v1/languages`): No authentication required
                                - **Admin APIs** (`/api/v1/admin/**`): Require `X-Admin-Key` header
                                - **Internal execution APIs** (`/api/v1/internal/**`): Require `X-Internal-Key` header

                                ## Pagination
                                List endpoints use `page` (0-based) and `size` (max 100) parameters.
                                Response format follows `PageResponse`:
                                ```json
                                {
                                  "content": [...],
                                  "page": 0,
                                  "size": 20,
                                  "totalElements": 100,
                                  "totalPages": 5
                                }
                                ```

                                ## Error Responses
                                All errors follow `ApiError` format:
                                ```json
                                {
                                  "code": "ERROR_CODE",
                                  "message": "Human readable message",
                                  "details": ["field: detail"]
                                }
                                ```

                                Common error codes:
                                - `NOT_FOUND` / `QUESTION_NOT_FOUND` / `TEST_CASE_NOT_FOUND` / `LANGUAGE_NOT_FOUND` / `COMPANY_NOT_FOUND` / `TOPIC_NOT_FOUND`
                                - `VALIDATION_FAILED`
                                - `CONFLICT` / `DUPLICATE_SLUG` / `DUPLICATE_NAME` / `STALE_VERSION` / `CATALOG_IN_USE`
                                - `ILLEGAL_STATUS_TRANSITION`
                                - `INTERNAL_ERROR`

                                ## Admin API Key
                                Set `ADMIN_API_KEY` environment variable. In development, defaults to `dev-admin-key-change-in-production`.
                                Include as header: `X-Admin-Key: your-admin-key`

                                Set `INTERNAL_API_KEY` to enable internal execution APIs.
                                Include as header: `X-Internal-Key: your-internal-key`
                                """)
                        .license(new License().name("Proprietary"))
                )
                .components(new Components()
                        .addSecuritySchemes("admin-key", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Admin-Key")
                                .description("Admin API key for accessing /api/v1/admin/** endpoints")
                        )
                        .addSecuritySchemes("internal-key", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Key")
                                .description("Internal API key for accessing /api/v1/internal/** endpoints")
                        )
                );
    }
}
