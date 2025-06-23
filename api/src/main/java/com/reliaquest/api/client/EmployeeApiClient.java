package com.reliaquest.api.client;

import com.reliaquest.api.constants.ClientConstants;
import com.reliaquest.api.constants.ExceptionConstants;
import com.reliaquest.api.exception.TooManyRequestsException;
import com.reliaquest.api.model.ApiResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmployeeApiClient {

    private final WebClient webClient;

    private <T> ApiResponse<T> getApiResponse(
            String uri, ParameterizedTypeReference<ApiResponse<T>> typeRef, Object... uriVars) {
        try {
            return webClient
                    .get()
                    .uri(uri, uriVars)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Too many requests while calling GET '{}'", uri);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during GET '{}' call", uri, e);
            throw e;
        }
    }

    private <T> ApiResponse<T> postApiResponse(
            String uri, Object body, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        try {
            return webClient
                    .post()
                    .uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Too many requests while calling POST '{}'", uri);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during POST '{}' call", uri, e);
            throw e;
        }
    }

    private <T> ApiResponse<T> deleteApiResponse(
            String uri, Object body, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        try {
            return webClient
                    .method(HttpMethod.DELETE)
                    .uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Too many requests while calling DELETE '{}'", uri);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during DELETE '{}' call", uri, e);
            throw e;
        }
    }

    @Retry(name = ClientConstants.EMPLOYEE_API_RETRY)
    public <T> T get(String uri, ParameterizedTypeReference<ApiResponse<T>> typeRef, Object... uriVars) {
        ApiResponse<T> response = getApiResponse(uri, typeRef, uriVars);

        if (response == null || response.getData() == null) {
            log.warn("GET '{}' returned null response or data", uri);
            throw new TooManyRequestsException(ExceptionConstants.EXC_TOO_MANY_REQUESTS_MESSAGE);
        }

        return response.getData();
    }

    @Retry(name = ClientConstants.EMPLOYEE_API_RETRY)
    public <T> T post(String uri, Object body, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        ApiResponse<T> response = postApiResponse(uri, body, typeRef);

        if (response == null || response.getData() == null) {
            log.warn("POST '{}' returned null response or data", uri);
            throw new TooManyRequestsException(ExceptionConstants.EXC_TOO_MANY_REQUESTS_MESSAGE);
        }

        return response.getData();
    }

    public <T> T delete(String uri, Object body, ParameterizedTypeReference<ApiResponse<T>> typeRef) {
        ApiResponse<T> response = deleteApiResponse(uri, body, typeRef);

        if (response == null || response.getData() == null) {
            log.warn("DELETE '{}' returned null response or data", uri);
            throw new TooManyRequestsException(ExceptionConstants.EXC_TOO_MANY_REQUESTS_MESSAGE);
        }

        return response.getData();
    }
}
