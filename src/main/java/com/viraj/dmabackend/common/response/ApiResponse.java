package com.viraj.dmabackend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    private String errorCode;

    private Integer status;

    private Instant timestamp;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public ApiResponse(boolean success, String message, T data, String errorCode, Integer status) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.status = status;
        this.timestamp = Instant.now();
    }
}
