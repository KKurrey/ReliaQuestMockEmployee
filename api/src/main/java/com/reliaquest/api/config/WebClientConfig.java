package com.reliaquest.api.config;

import com.reliaquest.api.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient employeeWebClient(@Value("${employee.api.base-url}") String baseUrl) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(ExceptionConstants.EXC_EMPLOYEE_API_BASE_URL_NULL);
        }

        log.info("Initializing WebClient with base URL: {}", baseUrl);

        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
