package com.viraj.dmabackend.exception;

import com.viraj.dmabackend.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNoResourceFound_ReturnsNotFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "static/missing.js");
        ResponseEntity<ApiResponse<Object>> response = handler.handleNoResourceFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleHttpMessageNotReadable_ReturnsBadRequest() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", (org.springframework.http.HttpInputMessage) null);
        ResponseEntity<ApiResponse<Object>> response = handler.handleHttpMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleMissingServletRequestParameter_ReturnsBadRequest() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("page", "int");
        ResponseEntity<ApiResponse<Object>> response = handler.handleMissingServletRequestParameter(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleMethodArgumentTypeMismatch_ReturnsBadRequest() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        ResponseEntity<ApiResponse<Object>> response = handler.handleMethodArgumentTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleHttpRequestMethodNotSupported_ReturnsMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<ApiResponse<Object>> response = handler.handleHttpRequestMethodNotSupported(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }
}
