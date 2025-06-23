package com.reliaquest.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.reliaquest.api.constants.ExceptionConstants;
import com.reliaquest.api.exception.TooManyRequestsException;
import com.reliaquest.api.model.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class EmployeeApiClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private EmployeeApiClient employeeApiClient;

    private final ParameterizedTypeReference<ApiResponse<String>> typeRef = new ParameterizedTypeReference<>() {};

    @BeforeEach
    void setUp() {
        // Mark as lenient to avoid unnecessary stubbing errors
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(webClient.method(HttpMethod.DELETE)).thenReturn(requestBodyUriSpec);
    }

    @Test
    void testGet_success() {
        ApiResponse<String> mockResponse = new ApiResponse<>();
        mockResponse.setData("Success");

        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.just(mockResponse));

        String result = employeeApiClient.get("/test", typeRef);
        assertEquals("Success", result);
    }

    @Test
    void testGet_nullResponse_shouldThrowTooManyRequestsException() {
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.empty());

        TooManyRequestsException ex =
                assertThrows(TooManyRequestsException.class, () -> employeeApiClient.get("/test", typeRef));
        assertEquals(ExceptionConstants.EXC_TOO_MANY_REQUESTS_MESSAGE, ex.getMessage());
    }

    @Test
    void testPost_success() {
        ApiResponse<String> mockResponse = new ApiResponse<>();
        mockResponse.setData("Posted");

        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.just(mockResponse));

        String result = employeeApiClient.post("/test", "body", typeRef);
        assertEquals("Posted", result);
    }

    @Test
    void testPost_nullResponse_shouldThrowTooManyRequestsException() {
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.empty());

        assertThrows(TooManyRequestsException.class, () -> employeeApiClient.post("/test", "body", typeRef));
    }

    @Test
    void testDelete_success() {
        ApiResponse<String> mockResponse = new ApiResponse<>();
        mockResponse.setData("Deleted");

        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.just(mockResponse));

        String result = employeeApiClient.delete("/test", "body", typeRef);
        assertEquals("Deleted", result);
    }

    @Test
    void testDelete_nullResponse_shouldThrowTooManyRequestsException() {
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenReturn(Mono.empty());

        assertThrows(TooManyRequestsException.class, () -> employeeApiClient.delete("/test", "body", typeRef));
    }

    @Test
    void testGet_tooManyRequests_shouldReturnNull() {
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(typeRef)).thenThrow(WebClientResponseException.TooManyRequests.class);

        TooManyRequestsException ex =
                assertThrows(TooManyRequestsException.class, () -> employeeApiClient.get("/test", typeRef));
        assertEquals(ExceptionConstants.EXC_TOO_MANY_REQUESTS_MESSAGE, ex.getMessage());
    }
}
